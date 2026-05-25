import React, { useEffect, useState } from 'react';
import { CarbonService, AreaService } from '../services/api';
import type { CarbonData, CampusArea } from '../types';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
    PieChart, Pie, Cell
} from 'recharts';
import { Activity, Filter } from 'lucide-react';  // 移除未使用的Leaf和CloudRain

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

const Dashboard: React.FC = () => {
    const [data, setData] = useState<CarbonData[]>([]);
    const [areas, setAreas] = useState<CampusArea[]>([]);
    const [selectedAreaId, setSelectedAreaId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);

    // Stats for the top cards
    const [stats, setStats] = useState({ total: 0, sequestration: 0, net: 0 });
    // Chart data
    const [emissionSourceData, setEmissionSourceData] = useState<{name: string, value: number}[]>([]);
    const [rankingData, setRankingData] = useState<{name: string, value: number}[]>([]);

    useEffect(() => {
        fetchAreas();
        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [selectedAreaId]);

    const fetchAreas = async () => {
        try {
            const res = await AreaService.getList();
            setAreas(res);
        } catch (e) {
            console.error("获取区域数据失败", e); // 中文错误日志
        }
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            let result;
            if (selectedAreaId) {
                result = await CarbonService.getRealTimeByArea(selectedAreaId);
                // Normalize single object to array for consistent processing
                result = [result];
            } else {
                result = await CarbonService.getRealTime();
            }

            const list = Array.isArray(result) ? result : [result];
            setData(list);
            processChartData(list);
        } catch (error) {
            console.error(error);
            // Fallback mock data for visualization if API fails (Dev experience)
            mockFallback();
        } finally {
            setLoading(false);
        }
    };

    const mockFallback = () => {
        const mock: CarbonData[] = [
            { id: 1, areaId: 1, areaName: '一号教学楼', totalCarbon: 120.5, sequestration: 0, netCarbon: 120.5, collectTime: '', createTime: '', emissionConsumes: [] },
            { id: 2, areaId: 2, areaName: '宿舍区', totalCarbon: 80.2, sequestration: 10, netCarbon: 70.2, collectTime: '', createTime: '', emissionConsumes: [] },
            { id: 3, areaId: 3, areaName: '绿化区', totalCarbon: 5.0, sequestration: 50, netCarbon: -45.0, collectTime: '', createTime: '', emissionConsumes: [] },
        ];
        setData(mock);
        processChartData(mock);
    };

    const processChartData = (currentData: CarbonData[]) => {
        let t = 0, s = 0, n = 0;
        const rank: {name: string, value: number}[] = [];
        const sources: {[key: string]: number} = {};

        currentData.forEach(item => {
            t += item.totalCarbon;
            s += item.sequestration;
            n += item.netCarbon;

            // Ensure area name exists or find it from areas list
            const areaName = item.areaName || areas.find(a => a.id === item.areaId)?.areaName || `区域 ${item.areaId}`; // 英文Area改为中文区域
            rank.push({ name: areaName, value: item.totalCarbon });

            item.emissionConsumes.forEach(ec => {
                if (!sources[ec.coefficientType]) sources[ec.coefficientType] = 0;
                sources[ec.coefficientType] += ec.carbonEmission;
            });
        });

        setStats({ total: t, sequestration: s, net: n });
        setRankingData(rank);
        setEmissionSourceData(Object.keys(sources).map(k => ({ name: k, value: Math.abs(sources[k]) })));
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row justify-between items-center bg-white p-4 rounded-xl shadow-sm border border-blue-100">
                <h2 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                    <Activity className="w-6 h-6 text-blue-600" />
                    实时监控
                </h2>
                <div className="flex items-center gap-2 mt-4 md:mt-0">
                    <Filter className="w-4 h-4 text-slate-500" />
                    <select
                        className="border border-blue-200 rounded-lg p-2 text-sm focus:ring-2 focus:ring-blue-400 outline-none"
                        value={selectedAreaId || ''}
                        onChange={(e) => setSelectedAreaId(e.target.value ? Number(e.target.value) : null)}
                    >
                        <option value="">所有区域</option>
                        {areas.map(a => (
                            <option key={a.id} value={a.id}>{a.areaName}</option>
                        ))}
                    </select>
                </div>
            </div>

            {/* Key Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-6 text-white shadow-lg">
                    <p className="text-blue-100 text-sm font-medium">总排放量</p>
                    <h3 className="text-4xl font-bold mt-2">{stats.total.toFixed(2)}</h3>
                    <p className="text-xs mt-1 opacity-80">千克二氧化碳</p>
                </div>
                <div className="bg-gradient-to-br from-emerald-500 to-emerald-600 rounded-xl p-6 text-white shadow-lg">
                    <p className="text-emerald-100 text-sm font-medium">碳固排量</p>
                    <h3 className="text-4xl font-bold mt-2">{stats.sequestration.toFixed(2)}</h3>
                    <p className="text-xs mt-1 opacity-80">千克二氧化碳</p>
                </div>
                <div className="bg-gradient-to-br from-indigo-500 to-indigo-600 rounded-xl p-6 text-white shadow-lg">
                    <p className="text-indigo-100 text-sm font-medium">净排放量</p>
                    <h3 className="text-4xl font-bold mt-2">{stats.net.toFixed(2)}</h3>
                    <p className="text-xs mt-1 opacity-80">千克二氧化碳</p>
                </div>
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Rankings */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-50">
                    <h3 className="text-lg font-semibold text-slate-700 mb-4">区域排放量排名</h3>
                    <div className="h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={rankingData} layout="vertical" margin={{ left: 20 }}>
                                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                                <XAxis type="number" unit=" 千克二氧化碳" /> {/* 补充X轴单位 */}
                                <YAxis dataKey="name" type="category" width={100} tick={{fontSize: 12}} />
                                <Tooltip cursor={{fill: '#f0f9ff'}} formatter={(value) => [`${value} 千克二氧化碳`, '排放量']} />
                                <Bar dataKey="value" fill="#3b82f6" radius={[0, 4, 4, 0]} barSize={20} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Source Distribution */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-50">
                    <h3 className="text-lg font-semibold text-slate-700 mb-4">排放来源构成</h3>
                    <div className="h-80">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie
                                    data={emissionSourceData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={60}
                                    outerRadius={100}
                                    fill="#8884d8"
                                    paddingAngle={5}
                                    dataKey="value"
                                    label
                                >
                                    {emissionSourceData.map((_, index) => (  // 修改为_避免未使用变量entry的警告
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip formatter={(value) => [`${value} 千克二氧化碳`, '排放量']} />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;