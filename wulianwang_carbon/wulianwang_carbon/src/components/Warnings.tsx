import React, { useEffect, useState } from 'react';
import { WarningService, AreaService } from '../services/api';
import type { WarningRecord, WarningQuery, CampusArea } from '../types';
import { CheckCircle, Clock, AlertOctagon } from 'lucide-react';
import { PieChart, Pie, Cell, Tooltip as RechartsTooltip, ResponsiveContainer } from 'recharts';

// 新增：警告类型映射函数（枚举值转中文）
const getWarningTypeCN = (type: string) => {
    switch (type) {
        case 'OVER_LIMIT':
            return '超限';
        case 'ABNORMAL_RISE':
            return '异常上升';
        default:
            return type;
    }
};

const Warnings: React.FC = () => {
    const [warnings, setWarnings] = useState<WarningRecord[]>([]);
    const [areas, setAreas] = useState<CampusArea[]>([]);
    const [query, setQuery] = useState<WarningQuery>({ pageNum: 1, pageSize: 10 });
    const [stats, setStats] = useState({ unhandled: 0, handled: 0 });

    // Handling
    const [handlingId, setHandlingId] = useState<number | null>(null);
    const [handleRemark, setHandleRemark] = useState('');

    useEffect(() => {
        loadData();
        loadAreas();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query]);

    const loadAreas = async () => {
        try {
            const res = await AreaService.getList();
            setAreas(res);
        } catch { /* 简化错误处理，避免未使用的e变量警告 */ }
    }

    const loadData = async () => {
        try {
            const res = await WarningService.getList(query);
            setWarnings(res.list);
            // 从列表计算统计数据（实际应用中，后端可能会提供摘要）
            setStats({
                unhandled: res.list.filter(w => w.handleStatus === 0).length,
                handled: res.list.filter(w => w.handleStatus === 1).length
            });
        } catch {
            // 模拟数据
            const mock: WarningRecord[] = [
                { id: 1, areaId: 2, areaName: "食堂", warningType: "OVER_LIMIT", warningTime: "2025-12-08 12:30:00", carbonValue: 1500, threshold: 1000, handleStatus: 0, handleTime: null, handleRemark: null },
                { id: 2, areaId: 1, areaName: "第一实验室", warningType: "ABNORMAL_RISE", warningTime: "2025-12-08 14:00:00", carbonValue: 2000, threshold: 1200, handleStatus: 1, handleTime: "2025-12-08 15:00:00", handleRemark: "已调查" }
            ];
            setWarnings(mock);
            setStats({ unhandled: 1, handled: 1 });
        }
    };

    const handleResolve = async () => {
        if (!handlingId || !handleRemark) return;
        try {
            await WarningService.handle(handlingId, handleRemark);
            setHandlingId(null);
            setHandleRemark('');
            loadData();
        } catch {
            alert("更新失败");
        }
    };

    const pieData = [
        { name: '未处理', value: stats.unhandled },
        { name: '已解决', value: stats.handled }
    ];

    return (
        <div className="space-y-6">
            {/* 顶部统计 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-xl shadow-sm border border-red-100 flex items-center justify-between">
                    <div>
                        <p className="text-red-500 font-medium">未处理警告</p>
                        <h3 className="text-3xl font-bold text-slate-800">{stats.unhandled}</h3>
                    </div>
                    <div className="p-3 bg-red-50 rounded-full">
                        <AlertOctagon className="w-8 h-8 text-red-500" />
                    </div>
                </div>
                <div className="bg-white p-6 rounded-xl shadow-sm border border-emerald-100 flex items-center justify-between">
                    <div>
                        <p className="text-emerald-500 font-medium">已解决问题</p>
                        <h3 className="text-3xl font-bold text-slate-800">{stats.handled}</h3>
                    </div>
                    <div className="p-3 bg-emerald-50 rounded-full">
                        <CheckCircle className="w-8 h-8 text-emerald-500" />
                    </div>
                </div>
            </div>

            {/* 主要内容 */}
            <div className="flex flex-col lg:flex-row gap-6">
                {/* 列表和筛选器 */}
                <div className="flex-1 bg-white p-6 rounded-xl shadow-sm border border-blue-50">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-bold text-slate-800">警告日志</h3>
                        <div className="flex gap-2">
                            <select
                                className="bg-slate-50 border border-slate-200 rounded px-2 py-1 text-sm outline-none"
                                onChange={e => setQuery({...query, warningType: e.target.value || undefined})}
                            >
                                <option value="">类型</option>
                                <option value="OVER_LIMIT">超限</option>
                                <option value="ABNORMAL_RISE">异常上升</option>
                            </select>
                            <select
                                className="bg-slate-50 border border-slate-200 rounded px-2 py-1 text-sm outline-none"
                                onChange={e => setQuery({...query, areaId: e.target.value ? Number(e.target.value) : undefined})}
                            >
                                <option value="">区域</option>
                                {areas.map(a => <option key={a.id} value={a.id}>{a.areaName}</option>)}
                            </select>
                        </div>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-sm text-left">
                            <thead className="bg-blue-50/50 text-slate-600 font-semibold">
                            <tr>
                                <th className="p-3 rounded-l-lg">编号</th>
                                <th className="p-3">区域</th>
                                <th className="p-3">类型</th>
                                <th className="p-3">数值/阈值</th>
                                <th className="p-3">状态</th>
                                <th className="p-3 rounded-r-lg">操作</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                            {warnings.map(w => (
                                <tr key={w.id} className="hover:bg-slate-50">
                                    <td className="p-3 text-slate-500">#{w.id}</td>
                                    <td className="p-3 font-medium text-slate-800">{w.areaName}</td>
                                    <td className="p-3">
                                        <span className="px-2 py-1 bg-orange-100 text-orange-700 rounded text-xs">{getWarningTypeCN(w.warningType)}</span>
                                    </td>
                                    <td className="p-3">
                                        <span className="text-red-600 font-bold">{w.carbonValue}</span> / <span className="text-slate-400">{w.threshold}</span>
                                    </td>
                                    <td className="p-3">
                                        {w.handleStatus === 0 ? (
                                            <span className="flex items-center gap-1 text-red-500 text-xs font-bold"><Clock className="w-3 h-3"/> 待处理</span>
                                        ) : (
                                            <span className="flex items-center gap-1 text-emerald-500 text-xs font-bold"><CheckCircle className="w-3 h-3"/> 已解决</span>
                                        )}
                                    </td>
                                    <td className="p-3">
                                        {w.handleStatus === 0 && (
                                            <button
                                                onClick={() => setHandlingId(w.id)}
                                                className="text-blue-600 hover:text-blue-800 underline text-xs"
                                            >
                                                处理
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* 图表侧边栏 */}
                <div className="w-full lg:w-80 space-y-6">
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-50 h-64">
                        <h4 className="text-sm font-bold text-slate-500 mb-2 uppercase">状态分布</h4>
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart>
                                <Pie data={pieData} innerRadius={40} outerRadius={60} paddingAngle={5} dataKey="value">
                                    <Cell fill="#ef4444" />
                                    <Cell fill="#10b981" />
                                </Pie>
                                <RechartsTooltip
                                    formatter={(value) => [`${value} 条`, '数量']} // 优化Tooltip提示
                                    labelFormatter={(label) => label}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* 处理模态框 */}
            {handlingId && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl p-6 w-96 shadow-2xl">
                        <h3 className="text-lg font-bold mb-4">处理警告 #{handlingId}</h3>
                        <textarea
                            className="w-full border border-slate-300 rounded-lg p-3 text-sm focus:ring-2 focus:ring-blue-500 outline-none"
                            rows={4}
                            placeholder="输入处理备注..."
                            value={handleRemark}
                            onChange={e => setHandleRemark(e.target.value)}
                        ></textarea>
                        <div className="flex justify-end gap-3 mt-4">
                            <button onClick={() => setHandlingId(null)} className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded">取消</button>
                            <button onClick={handleResolve} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">提交</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Warnings;