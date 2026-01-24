export type ApiResponse<T> = {
  data: T;
};

export type VehicleStatus =
  | "IN_LOT"
  | "IN_SERVICE"
  | "READY_FOR_DISTRIBUTION"
  | "DISTRIBUTED";

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
  purchasePrice: number;
  servicesTotal: number;
  totalCost: number;
  assignedPartnerName?: string | null;
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
  purchaseInvoiceDocumentId?: string | null;
  purchasePaymentReceiptDocumentId?: string | null;
  status: VehicleStatus;
  assignedPartnerId?: string | null;
  assignedPartnerName?: string | null;
  servicesTotal: number;
  totalCost: number;
  documentsCount: number;
};

export type ServiceItem = {
  id: string;
  vehicleId: string;
  serviceType: ServiceType;
  description?: string | null;
  serviceValue: number;
  performedAt?: string | null;
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
};

export type PartnerListResponse = {
  partners: PartnerItem[];
};

export type ReportVehicleItem = {
  vehicleId: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: number;
  purchasePrice: number;
  totalCost: number;
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
