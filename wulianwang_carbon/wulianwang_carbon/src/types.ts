// API Response Standard Wrapper
export interface ApiResponse<T> {
    code: number;
    msg: string;
    data: T;
}

// Constants instead of Enums (to avoid erasableSyntaxOnly error)
export const CoefficientType = {
    ELECTRICITY: 'ELECTRICITY',
    GAS: 'GAS',
    TREE: 'TREE',
    SOLAR: 'SOLAR',
    TRANSPORT: 'TRANSPORT'
} as const;

export type CoefficientType = typeof CoefficientType[keyof typeof CoefficientType];

export const WarningStatus = {
    UNHANDLED: 0,
    HANDLED: 1
} as const;

export type WarningStatus = typeof WarningStatus[keyof typeof WarningStatus];

// Data Models

export interface EmissionConsume {
    id: number | null;
    cdId: number;
    coefficientType: string;
    consumeAmount: number;
    carbonEmission: number;
    createTime: string;
    updateTime: string;
}

export interface CarbonData {
    id: number;
    areaId: number;
    areaName?: string; // Enhanced for UI convenience
    totalCarbon: number;
    sequestration: number;
    netCarbon: number;
    collectTime: string;
    createTime: string;
    emissionConsumes: EmissionConsume[];
}

export interface WarningRecord {
    id: number;
    areaId: number;
    areaName: string;
    warningType: string;
    warningTime: string;
    carbonValue: number;
    threshold: number;
    handleStatus: WarningStatus; // Changed to use type
    handleTime: string | null;
    handleRemark: string | null;
}

export interface CoefficientConfig {
    id: number;
    coefficientType: string;
    coefficientValue: number;
    remark: string;
    createTime?: string;
    updateTime?: string;
}

export interface CampusArea {
    id: number;
    areaName: string;
    areaType: string;
    areaSize: number; // String in JSON, logical number
    greenArea: number;
    plantType: string;
    location: string;
    status: number;
    createTime?: string;
    updateTime?: string;
}

export interface StatisticData {
    timeLabels: string[];
    totalCarbonList: number[];
    netCarbonList: number[];
    sourceLabels: string[];
    sourceValues: number[];
    areaLabels: string[];
    areaValues: number[];
}

// Request Payloads

export interface WarningQuery {
    areaId?: number;
    warningType?: string;
    handleStatus?: WarningStatus; // Changed to use type
    startTime?: string;
    endTime?: string;
    pageNum?: number;
    pageSize?: number;
}

export interface PaginatedResponse<T> {
    list: T[];
    total: number;
    pageNum: number;
    pageSize: number;
}

export interface CalculatePayload {
    [key: string]: number; // dynamic keys based on CoefficientType
}