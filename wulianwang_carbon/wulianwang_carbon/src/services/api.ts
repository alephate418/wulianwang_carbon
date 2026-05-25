import type {
    ApiResponse,
    CarbonData,
    StatisticData,
    WarningQuery,
    PaginatedResponse,
    WarningRecord,
    CoefficientConfig,
    CampusArea,
    CalculatePayload
} from '../types';

const BASE_URL = 'http://localhost:8080/api';

// Helper to simulate Axios-like behavior with Fetch
async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const resJson: ApiResponse<T> = await response.json();

        // In a real app, you might check resJson.code === 1 here
        return resJson.data;
    } catch (error) {
        console.error('API Request Failed:', error);
        throw error;
    }
}

export const CarbonService = {
    getRealTime: () => request<CarbonData[] | CarbonData>('/carbon/real-time'),
    getRealTimeByArea: (areaId: number) => request<CarbonData>(`/carbon/real-time/${areaId}`),
    getStatistic: (startDate: string, endDate: string) =>
        request<StatisticData>(`/carbon/statistic?startDate=${startDate}&endDate=${endDate}`),
    calculate: (areaId: number, data: CalculatePayload) =>
        request<CarbonData>(`/carbon/calculate?areaId=${areaId}`, {
            method: 'POST',
            body: JSON.stringify(data)
        })
};

export const WarningService = {
    getList: (params: WarningQuery) => {
        const query = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                query.append(key, value.toString());
            }
        });
        return request<PaginatedResponse<WarningRecord>>(`/warning/list?${query.toString()}`);
    },
    handle: (id: number, remark: string) =>
        request<boolean>(`/warning/handle/${id}?handleRemark=${encodeURIComponent(remark)}`, {
            method: 'PUT'
        })
};

export const CoefficientService = {
    getList: () => request<CoefficientConfig[]>('/coefficient/list'),
    getType: (type: string) => request<number>(`/coefficient/${type}`),
    add: (data: Partial<CoefficientConfig>) =>
        request<boolean>('/coefficient/add', { method: 'POST', body: JSON.stringify(data) }),
    update: (type: string, value: number) =>
        request<boolean>(`/coefficient/update?type=${type}&value=${value}`, { method: 'PUT' })
};

export const AreaService = {
    getList: () => request<CampusArea[]>('/area/list'),
    add: (data: Partial<CampusArea>) =>
        request<boolean>('/area/add', { method: 'POST', body: JSON.stringify(data) }),
    update: (data: Partial<CampusArea>) =>
        request<boolean>('/area/update', { method: 'PUT', body: JSON.stringify(data) })
};