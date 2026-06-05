export type ApiResponse<T> = {
  data: T;
};

export type VehicleStatus =
  | "IN_LOT"
  | "READY_FOR_DISTRIBUTION"
  | "DISTRIBUTED"
  | "SOLD";

export type SupplierSource = "INTERNET" | "PERSONAL_CONTACT";

export type ServiceType =
  | "MECHANICAL"
  | "PAINT"
  | "BODYWORK"
  | "ELECTRICAL"
  | "UPHOLSTERY"
  | "WINDOWS";

export type DocumentType = "INVOICE" | "RECEIPT" | "SERVICE_ORDER" | "OTHER";

export type VehicleListItem = {
  id: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  status: VehicleStatus;
  onService: boolean;
  sellingPrice?: number | null;
  totalCost: number;
  profitMargin?: number | null;
  purchaseTimeDays?: number | null;
  servicesTotal: number;
  daysOnService?: number | null;
};

export type VehicleListResponse = {
  items: VehicleListItem[];
  page: number;
  size: number;
  total: number;
};

export type VehicleDetail = {
  id: string;
  licensePlate: string;
  renavam?: string | null;
  vin?: string | null;
  year: number;
  color: string;
  model: string;
  brand: string;
  supplierSource: SupplierSource;
  purchasePrice: number;
  freightCost: number;
  purchaseCommission: number;
  sellingPrice?: number | null;
  purchaseInvoiceDocumentId?: string | null;
  purchasePaymentReceiptDocumentId?: string | null;
  status: VehicleStatus;
  onService: boolean;
  assignedPartnerId?: string | null;
  assignedPartnerName?: string | null;
  servicesTotal: number;
  totalCost: number;
  documentsCount: number;
  distributedAt?: string | null;
};

export type SoldVehicleItem = {
  vehicleId: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  soldAt: string;
  sellingPrice: number;
  totalTaxes: number;
  servicesTotal: number;
  purchaseCommission: number;
  profit: number;
  commissionRate: number | null;
};

export type SoldVehiclesReport = {
  vehicles: SoldVehicleItem[];
  totalVehiclesSold: number;
  totalSoldValue: number;
  totalTaxesValue: number;
  totalServiceValue: number;
  totalCommissionValue: number;
  profit: number;
};

export type ServiceItem = {
  id: string;
  vehicleId: string;
  serviceType: ServiceType;
  description?: string | null;
  serviceValue: number;
  startDate: string;
  endDate?: string | null;
};

export type ServiceListResponse = {
  services: ServiceItem[];
  total: number;
};

export type DocumentItem = {
  id: string;
  vehicleId: string;
  documentType: DocumentType;
  originalFileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedAt: string;
};

export type DocumentListResponse = {
  documents: DocumentItem[];
};

export type PartnerItem = {
  id: string;
  name: string;
  city?: string | null;
  phone?: string | null;
  email?: string | null;
  commissionRate?: number | null;
};

export type PartnerListResponse = {
  partners: PartnerItem[];
};

export type PartnerDetail = {
  id: string;
  name: string;
  city?: string | null;
  phone?: string | null;
  email?: string | null;
  commissionRate?: number | null;
};

export type PartnerHistoryItem = {
  id: string;
  name: string;
  city?: string | null;
  phone?: string | null;
  email?: string | null;
  commissionRate?: number | null;
  changedAt: string;
  changedBy?: string | null;
};

export type PartnerHistoryResponse = {
  history: PartnerHistoryItem[];
};

export type BrandItem = {
  id: string;
  name: string;
};

export type BrandListResponse = {
  brands: BrandItem[];
};

export type ModelItem = {
  id: string;
  name: string;
};

export type ModelListResponse = {
  models: ModelItem[];
};

export type ColorItem = {
  id: string;
  name: string;
};

export type ColorListResponse = {
  colors: ColorItem[];
};

export type ReportVehicleItem = {
  vehicleId: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  distributedAt?: string | null;
  purchasePrice: number;
  purchaseCommission: number;
  totalCost: number;
};

export type VehicleTaxes = {
  icms: number;
  pis: number;
  cofins: number;
  csll: number;
  irpj: number;
  totalTaxes: number;
};

export type ReportPartnerGroup = {
  partnerId: string;
  partnerName: string;
  vehicles: ReportVehicleItem[];
  partnerVehiclesTotalValue: number;
  partnerVehiclesCount: number;
};

export type DistributedVehiclesReport = {
  partners: ReportPartnerGroup[];
  overallVehiclesCount: number;
  overallVehiclesTotalValue: number;
};

export enum PaymentType {
  WARRANTY = "WARRANTY",
  OPERATIONAL_COST = "OPERATIONAL_COST",
  PRO_LABORE = "PRO_LABORE",
  BONUS_PLR = "BONUS_PLR",
  OTHER = "OTHER",
}

export type PaymentItem = {
  id: string;
  paymentType: PaymentType;
  description?: string | null;
  amount: number;
  paymentDate: string;
  vehicleId?: string | null;
  vehicleLicensePlate?: string | null;
  notes?: string | null;
  createdAt: string;
  updatedAt?: string | null;
};

export type PaymentListResponse = {
  payments: PaymentItem[];
};

export type PaymentDocumentItem = {
  id: string;
  paymentId: string;
  originalFileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedAt: string;
};

export type PaymentDocumentListResponse = {
  documents: PaymentDocumentItem[];
};

export type FinancialMonthlyPoint = {
  year: number;
  month: number;
  salesProfit: number;
  expenses: number;
  net: number;
};

export type FinancialDashboardData = {
  cashBalanceAmount: number;
  valorEmCaixa: number;
  patrimonio: number;
  activeVehiclesCount: number;
  activeVehiclesTotalCost: number;
  lucroVendas: number;
  totalVehiclesSold: number;
  lucroCompras: number;
  totalVehiclesAcquired: number;
  totalPayments: number;
  monthlyEvolution: FinancialMonthlyPoint[];
};

export type CashBalanceData = {
  amount: number;
  updatedAt: string;
};

export type CreatePaymentRequest = {
  paymentType: PaymentType;
  description?: string;
  amount: number;
  paymentDate: string;
  vehicleLicensePlate?: string;
  notes?: string;
};

export type UpdatePaymentRequest = CreatePaymentRequest;
