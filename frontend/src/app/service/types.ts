export type ApiResponse<T> = {
  data: T;
};

export type StatusVeiculo =
  | "NO_PATIO"
  | "PRONTO_PARA_DISTRIBUICAO"
  | "DISTRIBUIDO"
  | "VENDIDO";

export type OrigemFornecedor = "INTERNET" | "CONTATO_PESSOAL";

export type TipoServico =
  | "MECANICA"
  | "PINTURA"
  | "FUNILARIA"
  | "ELETRICA"
  | "ESTOFAMENTO"
  | "VIDROS";

export type TipoDocumento = "NOTA_FISCAL" | "COMPROVANTE" | "ORDEM_SERVICO" | "OUTRO";

export type ItemListaVeiculo = {
  id: string;
  placa: string;
  marca: string;
  modelo: string;
  ano: number;
  status: StatusVeiculo;
  emServico: boolean;
  precoVenda?: number | null;
  custoTotal: number;
  margemLucro?: number | null;
  diasTempoCompra?: number | null;
  totalServicos: number;
  diasEmServico?: number | null;
};

export type ListaVeiculosResponse = {
  items: ItemListaVeiculo[];
  page: number;
  size: number;
  total: number;
};

export type DetalheVeiculo = {
  id: string;
  placa: string;
  renavam?: string | null;
  chassi?: string | null;
  ano: number;
  cor: string;
  modelo: string;
  marca: string;
  origemFornecedor: OrigemFornecedor;
  precoCompra: number;
  custoFrete: number;
  comissaoCompra: number;
  precoVenda?: number | null;
  valorFipe?: number | null;
  documentoNotaFiscalCompraId?: string | null;
  documentoComprovantePagamentoCompraId?: string | null;
  status: StatusVeiculo;
  emServico: boolean;
  parceiroAtribuidoId?: string | null;
  nomeParceiroAtribuido?: string | null;
  totalServicos: number;
  custoTotal: number;
  documentosCount: number;
  distribuidoEm?: string | null;
};

export type ItemVeiculoVendido = {
  veiculoId: string;
  placa: string;
  marca: string;
  modelo: string;
  ano: number;
  dataVenda: string;
  precoCompra: number;
  precoVenda: number;
  totalImpostos: number;
  totalServicos: number;
  comissaoCompra: number;
  lucro: number;
  taxaComissao: number | null;
};

export type RelatorioVeiculosVendidos = {
  veiculos: ItemVeiculoVendido[];
  totalVeiculosVendidos: number;
  valorTotalVendido: number;
  valorTotalImpostos: number;
  valorTotalServicos: number;
  valorTotalComissoes: number;
  lucro: number;
  lucroAntesImpostos: number;
};

export type ServicoItem = {
  id: string;
  veiculoId: string;
  tipoServico: TipoServico;
  descricao?: string | null;
  valorServico: number;
  dataInicio: string;
  dataFim?: string | null;
};

export type ListaServicosResponse = {
  servicos: ServicoItem[];
  total: number;
};

export type DocumentoItem = {
  id: string;
  veiculoId: string;
  tipoDocumento: TipoDocumento;
  nomeOriginalArquivo: string;
  contentType: string;
  tamanhoBytes: number;
  enviadoEm: string;
};

export type ListaDocumentosResponse = {
  documentos: DocumentoItem[];
};

export type ParceiroItem = {
  id: string;
  nome: string;
  cidade?: string | null;
  telefone?: string | null;
  email?: string | null;
  taxaComissao?: number | null;
};

export type ListaParceirosResponse = {
  parceiros: ParceiroItem[];
};

export type DetalheParceiro = {
  id: string;
  nome: string;
  cidade?: string | null;
  telefone?: string | null;
  email?: string | null;
  taxaComissao?: number | null;
};

export type HistoricoParceiroItem = {
  id: string;
  nome: string;
  cidade?: string | null;
  telefone?: string | null;
  email?: string | null;
  taxaComissao?: number | null;
  alteradoEm: string;
  alteradoPor?: string | null;
};

export type HistoricoParceiroResponse = {
  historico: HistoricoParceiroItem[];
};

export type MarcaItem = {
  id: string;
  nome: string;
};

export type ListaMarcasResponse = {
  marcas: MarcaItem[];
};

export type ModeloItem = {
  id: string;
  nome: string;
};

export type ModeloListResponse = {
  modelos: ModeloItem[];
};

export type CorItem = {
  id: string;
  nome: string;
};

export type ListaCoresResponse = {
  cores: CorItem[];
};

export type ItemVeiculoRelatorio = {
  veiculoId: string;
  placa: string;
  marca: string;
  modelo: string;
  ano: number;
  distribuidoEm?: string | null;
  precoCompra: number;
  comissaoCompra: number;
  custoTotal: number;
};

export type ImpostosVeiculo = {
  icms: number;
  pis: number;
  cofins: number;
  csll: number;
  irpj: number;
  totalImpostos: number;
};

export type GrupoParceiroRelatorio = {
  parceiroId: string;
  nomeParceiro: string;
  veiculos: ItemVeiculoRelatorio[];
  valorTotalVeiculosParceiro: number;
  quantidadeVeiculosParceiro: number;
};

export type RelatorioVeiculosDistribuidos = {
  parceiros: GrupoParceiroRelatorio[];
  quantidadeGeralVeiculos: number;
  valorTotalGeralVeiculos: number;
};

export enum TipoPagamento {
  GARANTIA = "GARANTIA",
  CUSTO_OPERACIONAL = "CUSTO_OPERACIONAL",
  PRO_LABORE = "PRO_LABORE",
  BONUS_PLR = "BONUS_PLR",
  OUTRO = "OUTRO",
}

export type PagamentoItem = {
  id: string;
  tipoPagamento: TipoPagamento;
  descricao?: string | null;
  valor: number;
  dataPagamento: string;
  veiculoId?: string | null;
  veiculoPlaca?: string | null;
  observacoes?: string | null;
  criadoEm: string;
  atualizadoEm?: string | null;
};

export type ListaPagamentosResponse = {
  pagamentos: PagamentoItem[];
};

export type DocumentoPagamentoItem = {
  id: string;
  pagamentoId: string;
  nomeOriginalArquivo: string;
  contentType: string;
  tamanhoBytes: number;
  enviadoEm: string;
};

export type ListaDocumentosPagamentoResponse = {
  documentos: DocumentoPagamentoItem[];
};

export type PontoMensalFinanceiro = {
  ano: number;
  mes: number;
  lucroVendas: number;
  despesas: number;
  net: number;
};

export type PainelFinanceiroData = {
  valorSaldoCaixa: number;
  valorEmCaixa: number;
  patrimonio: number;
  quantidadeVeiculosAtivos: number;
  custoTotalVeiculosAtivos: number;
  lucroVendas: number;
  lucroVendasSemImpostos: number;
  totalVeiculosVendidos: number;
  lucroCompras: number;
  totalVeiculosAdquiridos: number;
  totalPagamentos: number;
  evolucaoMensal: PontoMensalFinanceiro[];
};

export type SaldoCaixaData = {
  valor: number;
  atualizadoEm: string;
};

export type CriarPagamentoRequest = {
  tipoPagamento: TipoPagamento;
  descricao?: string;
  valor: number;
  dataPagamento: string;
  veiculoPlaca?: string;
  observacoes?: string;
};

export type AtualizarPagamentoRequest = CriarPagamentoRequest;
