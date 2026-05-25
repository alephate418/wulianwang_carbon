import React, { useEffect, useState } from 'react';
import { AreaService } from '../services/api';
import type { CampusArea } from '../types';
import { MapPin, Trees, Layers, Plus } from 'lucide-react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'];

const Areas: React.FC = () => {
    const [areas, setAreas] = useState<CampusArea[]>([]);

    const loadData = async () => {
        try {
            const res = await AreaService.getList();
            setAreas(res);
        } catch (_) {
            // Mock
            setAreas([
                { id: 1, areaName: "一号教学楼", areaType: "教学", areaSize: 8000, greenArea: 0, plantType: "", location: "30.67,104.06", status: 1 },
                { id: 2, areaName: "宿舍区", areaType: "宿舍", areaSize: 12000, greenArea: 200, plantType: "Grass", location: "30.68,104.07", status: 1 },
                { id: 3, areaName: "绿化区", areaType: "绿化", areaSize: 5000, greenArea: 5000, plantType: "Trees", location: "30.68,104.89", status: 1 },
            ]);
        }
    };

    useEffect(() => {
        void loadData();
    }, []);

    const typeData = React.useMemo(() => {
        const counts: {[key: string]: number} = {};
        areas.forEach(a => {
            counts[a.areaType] = (counts[a.areaType] || 0) + 1;
        });
        return Object.keys(counts).map(k => ({ name: k, value: counts[k] }));
    }, [areas]);

    return (
        <div className="space-y-6">
            <div className="flex flex-col md:flex-row gap-6">
                {/* Map Placeholder & Summary */}
                <div className="flex-1 bg-white rounded-xl shadow-sm border border-blue-50 p-6 flex flex-col">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="font-bold text-slate-800 flex items-center gap-2">
                            <MapPin className="w-5 h-5 text-red-500" /> 区域地图
                        </h3>
                        <button className="text-xs bg-blue-50 text-blue-600 px-2 py-1 rounded border border-blue-100">+ 添加地图</button>
                    </div>
                    <div className="flex-1 bg-blue-50/50 rounded-lg flex items-center justify-center min-h-[250px] relative overflow-hidden group">
                        <div className="absolute inset-0 opacity-10 bg-[url('https://picsum.photos/800/400')] bg-cover bg-center"></div>
                        <p className="text-blue-300 font-bold z-10">交互式地图可视化</p>
                        {/* Simulated Pins */}
                        {areas.map((a, i) => (
                            <div key={a.id} className="absolute w-3 h-3 bg-red-500 rounded-full border-2 border-white shadow-lg animate-pulse"
                                 style={{ top: `${30 + (i * 20)}%`, left: `${20 + (i * 25)}%` }}
                                 title={a.areaName}
                            />
                        ))}
                    </div>
                </div>

                {/* Stats */}
                <div className="w-full md:w-80 bg-white rounded-xl shadow-sm border border-blue-50 p-6">
                    <h3 className="font-bold text-slate-800 mb-4">区域类型</h3>
                    <div className="h-48">
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie data={typeData} innerRadius={40} outerRadius={70} dataKey="value" paddingAngle={5}>
                                    {typeData.map((_, index) => (
                                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend verticalAlign="bottom" height={36}/>
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {areas.map(area => (
                    <div key={area.id} className="bg-white rounded-xl shadow-sm border border-blue-100 p-5 hover:shadow-md transition-shadow">
                        <div className="flex justify-between items-start mb-3">
                            <h4 className="font-bold text-lg text-slate-800">{area.areaName}</h4>
                            <span className={`px-2 py-1 rounded text-xs font-bold ${area.areaType === 'Green' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'}`}>
                {area.areaType}
              </span>
                        </div>
                        <div className="space-y-2 text-sm text-slate-600">
                            <div className="flex items-center gap-2">
                                <Layers className="w-4 h-4 text-slate-400" />
                                <span>总面积: <span className="font-semibold text-slate-800">{area.areaSize} m²</span></span>
                            </div>
                            <div className="flex items-center gap-2">
                                <Trees className="w-4 h-4 text-slate-400" />
                                <span>绿化面积: <span className="font-semibold text-slate-800">{area.greenArea} m²</span></span>
                            </div>
                            <div className="flex items-center gap-2">
                                <MapPin className="w-4 h-4 text-slate-400" />
                                <span className="truncate w-full">{area.location}</span>
                            </div>
                        </div>
                    </div>
                ))}

                {/* Add Card */}
                <button className="border-2 border-dashed border-blue-200 rounded-xl p-5 flex flex-col items-center justify-center text-blue-400 hover:bg-blue-50 transition-colors h-full min-h-[160px]">
                    <Plus className="w-8 h-8 mb-2" />
                    <span className="font-medium">注册新区域</span>
                </button>
            </div>
        </div>
    );
};

export default Areas;