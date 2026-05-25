import React, { useState } from 'react';
import { CarbonService } from '../services/api';
import type { StatisticData } from '../types';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { TrendingUp, Calendar, Search } from 'lucide-react';

const Statistics: React.FC = () => {
    const [dates, setDates] = useState({ start: '', end: '' });
    const [data, setData] = useState<StatisticData | null>(null);
    const [loading, setLoading] = useState(false);

    const handleSearch = async () => {
        if (!dates.start || !dates.end) return;
        setLoading(true);
        try {
            const res = await CarbonService.getStatistic(dates.start, dates.end);
            setData(res);
        } catch (e) {
            console.error("获取统计数据失败", e); // 补充中文错误日志
            // Mock data for demo
            setData({
                timeLabels: ['2023-12-01', '2023-12-02', '2023-12-03', '2023-12-04'],
                totalCarbonList: [1200, 1300, 1150, 1400],
                netCarbonList: [1000, 1100, 950, 1200],
                sourceLabels: [], sourceValues: [], areaLabels: [], areaValues: []
            });
        } finally {
            setLoading(false);
        }
    };

    const formattedChartData = data?.timeLabels.map((label, index) => ({
        time: label,
        total: data.totalCarbonList[index],
        net: data.netCarbonList[index]
    })) || [];

    return (
        <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-100">
                <h2 className="text-2xl font-bold text-slate-800 flex items-center gap-2 mb-6">
                    <TrendingUp className="w-6 h-6 text-blue-600" />
                    趋势分析
                </h2>

                <div className="flex flex-wrap gap-4 items-end bg-blue-50/50 p-4 rounded-lg">
                    <div>
                        <label className="block text-sm font-medium text-slate-600 mb-1">开始日期</label>
                        <div className="relative">
                            <Calendar className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
                            <input
                                type="date"
                                className="pl-9 pr-4 py-2 rounded-lg border border-slate-200 focus:ring-2 focus:ring-blue-400 outline-none w-full"
                                value={dates.start}
                                onChange={(e) => setDates({...dates, start: e.target.value})}
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-slate-600 mb-1">结束日期</label>
                        <div className="relative">
                            <Calendar className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
                            <input
                                type="date"
                                className="pl-9 pr-4 py-2 rounded-lg border border-slate-200 focus:ring-2 focus:ring-blue-400 outline-none w-full"
                                value={dates.end}
                                onChange={(e) => setDates({...dates, end: e.target.value})}
                            />
                        </div>
                    </div>
                    <button
                        onClick={handleSearch}
                        disabled={loading}
                        className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg flex items-center gap-2 transition-colors"
                    >
                        <Search className="w-4 h-4" />
                        {loading ? '分析中...' : '分析'}
                    </button>
                </div>
            </div>

            {data && (
                <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-50">
                    <h3 className="text-lg font-semibold text-slate-700 mb-6">碳排放趋势</h3>
                    <div className="h-96">
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={formattedChartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                <XAxis dataKey="time" stroke="#64748b" />
                                <YAxis stroke="#64748b" unit=" 千克二氧化碳" /> {/* 补充Y轴单位 */}
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: '1px solid #e2e8f0' }}
                                    formatter={(value) => [`${value} 千克二氧化碳`, '排放量']} // 优化Tooltip提示
                                    labelFormatter={(label) => `日期: ${label}`}
                                />
                                <Legend />
                                <Line
                                    type="monotone"
                                    dataKey="total"
                                    stroke="#3b82f6"
                                    strokeWidth={3}
                                    name="总排放量"
                                    dot={{ r: 4, fill: '#3b82f6' }}
                                    activeDot={{ r: 8 }}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="net"
                                    stroke="#10b981"
                                    strokeWidth={3}
                                    name="净排放量"
                                    dot={{ r: 4, fill: '#10b981' }}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Statistics;