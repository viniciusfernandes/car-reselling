#!/usr/bin/env bash
# =============================================================================
# load-env.sh
#
# Manages environment variables from a .env file across three scopes:
#
#   SESSION  – export into the current terminal only (lost when terminal closes)
#   GLOBAL   – add a source line to ~/.bashrc / ~/.zshrc so every new terminal
#              automatically loads the variables on startup
#   REMOVE   – remove that source line from the RC file
#
# ── Modes ────────────────────────────────────────────────────────────────────
#
#   source ./load-env.sh [--session]       [<env-file>]   # default
#   source ./load-env.sh --global          [<env-file>]   # also writes to RC
#          ./load-env.sh --remove-global   [<env-file>]
#          ./load-env.sh --status          [<env-file>]
#   source ./load-env.sh --unset-session   [<env-file>]
#
# ── Examples ─────────────────────────────────────────────────────────────────
#
#   source ./load-env.sh                        # session: load .env
#   source ./load-env.sh .env.development       # session: load other file
#   source ./load-env.sh --global               # write to RC + export now
#   source ./load-env.sh --global .env.development
#          ./load-env.sh --remove-global        # remove from RC file
#          ./load-env.sh --status               # show what is/isn't in RC
#
# After --global, open a new terminal (or run: source ~/.bashrc)
# =============================================================================

# ---------------------------------------------------------------------------
# Internal helpers
# ---------------------------------------------------------------------------

# Returns the absolute path of a file (resolves relative paths)
_env_abspath() {
    local f="$1"
    # If it's already absolute, use it; otherwise prepend $PWD
    if [[ "$f" = /* ]]; then
        echo "$f"
    else
        echo "$PWD/$f"
    fi
}

# Detect the user's "main" interactive shell RC file
_env_detect_rc() {
    local shell_name
    shell_name="$(basename "${SHELL:-bash}")"
    case "$shell_name" in
        zsh)  echo "${ZDOTDIR:-$HOME}/.zshrc" ;;
        bash)
            if [[ -f "$HOME/.bashrc" ]]; then
                echo "$HOME/.bashrc"
            else
                echo "$HOME/.bash_profile"
            fi
            ;;
        ksh)  echo "$HOME/.kshrc" ;;
        fish) echo "$HOME/.config/fish/config.fish" ;;
        *)    echo "$HOME/.profile" ;;
    esac
}

# Parse one env line → sets _KEY and _VALUE; returns 1 if line should be skipped
_env_parse_line() {
    local line="$1"
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && return 1

    local stripped="${line#export }"
    [[ "$stripped" =~ ^[A-Za-z_][A-Za-z0-9_]*= ]] || return 1

    _KEY="${stripped%%=*}"
    _VALUE="${stripped#*=}"

    # Strip surrounding quotes
    if   [[ "$_VALUE" =~ ^\"(.*)\"$ ]]; then _VALUE="${BASH_REMATCH[1]}"
    elif [[ "$_VALUE" =~ ^\'(.*)\'$ ]]; then _VALUE="${BASH_REMATCH[1]}"
    fi
    return 0
}

# The unique tag written into the RC file (one tag per env-file path)
_env_rc_tag() { echo "car-reselling:$1"; }

# ---------------------------------------------------------------------------
# --session (default): export into the current shell only
# ---------------------------------------------------------------------------
_env_load_session() {
    local env_file="${1:-.env}"
    [[ ! -f "$env_file" ]] && { echo "load-env: file not found: $env_file" >&2; return 1; }

    local loaded=0 _KEY _VALUE
    while IFS= read -r line || [[ -n "$line" ]]; do
        _env_parse_line "$line" || continue
        export "$_KEY=$_VALUE"
        (( loaded++ ))
    done < "$env_file"

    echo "load-env [session]: exported $loaded variable(s) from $env_file"
}

# ---------------------------------------------------------------------------
# --global: add one source line to the RC file and also export now
#
# The line added looks like:
#   source "/abs/path/load-env.sh" "/abs/path/.env"  # car-reselling:<tag>
#
# This way the RC file stays clean (one line per env file), and updating .env
# automatically takes effect in new terminals without touching the RC file again.
# ---------------------------------------------------------------------------
_env_load_global() {
    local env_file="${1:-.env}"
    [[ ! -f "$env_file" ]] && { echo "load-env: file not found: $env_file" >&2; return 1; }

    local abs_env abs_script rc_file tag source_line
    abs_env="$(_env_abspath "$env_file")"
    abs_script="$(_env_abspath "${BASH_SOURCE[0]:-$0}")"
    rc_file="$(_env_detect_rc)"
    tag="$(_env_rc_tag "$abs_env")"
    source_line="source \"$abs_script\" \"$abs_env\"  # $tag"

    # Ensure RC file exists
    touch "$rc_file" 2>/dev/null || {
        echo "load-env [global]: cannot write to $rc_file — permission denied" >&2
        return 1
    }

    # Remove any previously written line for this env file (idempotent)
    if grep -qF "# $tag" "$rc_file" 2>/dev/null; then
        local tmp
        tmp="$(mktemp)" || { echo "load-env [global]: mktemp failed" >&2; return 1; }
        grep -vF "# $tag" "$rc_file" > "$tmp" && mv "$tmp" "$rc_file" || {
            rm -f "$tmp"
            echo "load-env [global]: failed to update $rc_file" >&2
            return 1
        }
        echo "load-env [global]: replaced existing entry in $rc_file"
    fi

    # Append the new source line
    printf '\n%s\n' "$source_line" >> "$rc_file" || {
        echo "load-env [global]: failed to write to $rc_file" >&2
        return 1
    }

    # Also export into the current session right now
    local loaded=0 _KEY _VALUE
    while IFS= read -r line || [[ -n "$line" ]]; do
        _env_parse_line "$line" || continue
        export "$_KEY=$_VALUE"
        (( loaded++ ))
    done < "$env_file"

    echo "load-env [global]: added source line to $rc_file"
    echo "load-env [global]: exported $loaded variable(s) to current session"
    echo ""
    echo "  New terminals will load variables automatically."
    echo "  To apply in existing open terminals run:"
    echo "    source $rc_file"
}

# ---------------------------------------------------------------------------
# --remove-global: delete the source line from the RC file
# ---------------------------------------------------------------------------
_env_remove_global() {
    local env_file="${1:-.env}"
    local abs_env rc_file tag

    abs_env="$(_env_abspath "$env_file")"
    rc_file="$(_env_detect_rc)"
    tag="$(_env_rc_tag "$abs_env")"

    if ! grep -qF "# $tag" "$rc_file" 2>/dev/null; then
        echo "load-env [remove-global]: no entry found for '$env_file' in $rc_file"
        return 0
    fi

    local tmp
    tmp="$(mktemp)" || { echo "load-env [remove-global]: mktemp failed" >&2; return 1; }
    grep -vF "# $tag" "$rc_file" > "$tmp" && mv "$tmp" "$rc_file" || {
        rm -f "$tmp"
        echo "load-env [remove-global]: failed to update $rc_file" >&2
        return 1
    }

    echo "load-env [remove-global]: removed entry for '$env_file' from $rc_file"
    echo ""
    echo "  Variables are still active in open terminals."
    echo "  To unset them in the current session:"
    echo "    source ./load-env.sh --unset-session"
}

# ---------------------------------------------------------------------------
# --unset-session: unset vars from the current shell session only
# ---------------------------------------------------------------------------
_env_unset_session() {
    local env_file="${1:-.env}"
    [[ ! -f "$env_file" ]] && { echo "load-env: file not found: $env_file" >&2; return 1; }

    local count=0 _KEY _VALUE
    while IFS= read -r line || [[ -n "$line" ]]; do
        _env_parse_line "$line" || continue
        unset "$_KEY"
        (( count++ ))
    done < "$env_file"

    echo "load-env [unset-session]: unset $count variable(s) from current session"
}

# ---------------------------------------------------------------------------
# --status: show which variables are in the RC file and/or current session
# ---------------------------------------------------------------------------
_env_status() {
    local env_file="${1:-.env}"
    [[ ! -f "$env_file" ]] && { echo "load-env: file not found: $env_file" >&2; return 1; }

    local abs_env rc_file tag
    abs_env="$(_env_abspath "$env_file")"
    rc_file="$(_env_detect_rc)"
    tag="$(_env_rc_tag "$abs_env")"

    echo "load-env [status]"
    echo "  env file : $abs_env"
    echo "  RC file  : $rc_file"

    if grep -qF "# $tag" "$rc_file" 2>/dev/null; then
        echo "  global   : ✓ entry is present in $rc_file"
        echo "  source line:"
        grep -F "# $tag" "$rc_file" | sed 's/^/    /'
    else
        echo "  global   : ✗ NOT in $rc_file  (run: source ./load-env.sh --global)"
    fi

    echo ""
    printf "  %-40s %-12s\n" "VARIABLE" "IN SESSION"
    printf "  %-40s %-12s\n" "--------" "----------"

    local _KEY _VALUE
    while IFS= read -r line || [[ -n "$line" ]]; do
        _env_parse_line "$line" || continue
        local in_session="no"
        [[ -n "${!_KEY+x}" ]] && in_session="yes"
        printf "  %-40s %-12s\n" "$_KEY" "$in_session"
    done < "$env_file"
}

# =============================================================================
# Entry point
# =============================================================================
case "${1:-}" in
    --global)
        _env_load_global "${2:-.env}"
        ;;
    --remove-global)
        _env_remove_global "${2:-.env}"
        ;;
    --unset-session | --unset)
        _env_unset_session "${2:-.env}"
        ;;
    --status)
        _env_status "${2:-.env}"
        ;;
    --session)
        _env_load_session "${2:-.env}"
        ;;
    *)
        # No flag or a file path was given → session mode
        _env_load_session "${1:-.env}"
        ;;
esac

unset -f _env_abspath _env_detect_rc _env_parse_line _env_rc_tag \
         _env_load_session _env_load_global _env_remove_global \
         _env_unset_session _env_status
