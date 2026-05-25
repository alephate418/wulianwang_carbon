import React, { useEffect, useState } from 'react';
import { CoefficientService } from '../services/api';
import type { CoefficientConfig } from '../types';
import { Settings, Plus, Edit2, Save, X } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const Coefficients: React.FC = () => {
    const [coeffs, setCoeffs] = useState<CoefficientConfig[]>([]);
    const [editing, setEditing] = useState<Partial<CoefficientConfig> | null>(null);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const res = await CoefficientService.getList();
            setCoeffs(res);
        } catch (e) {
            console.error(e);
            // Mock 数据（已替换为中文）
            setCoeffs([
                { id: 1, coefficientType: "用电", coefficientValue: 0.58, remark: "电网 (千克二氧化碳/千瓦时)" },
                { id: 2, coefficientType: "天然气", coefficientValue: 2.1, remark: "天然气 (千克二氧化碳/立方米)" },
                { id: 3, coefficientType: "树木固碳", coefficientValue: -0.05, remark: "树木固碳 (千克二氧化碳/天)" }
            ]);
        }
    };

    const handleSave = async () => {
        if (!editing) return;
        try {
            if (editing.id) {
                // 更新操作
                // 注：PDF中的API使用/coefficient/update查询参数，但本地状态通常包含完整对象
                // 这里匹配服务端签名
                await CoefficientService.update(editing.coefficientType!, editing.coefficientValue!);
            } else {
                // 新增操作
                await CoefficientService.add(editing);
            }
            setEditing(null);
            loadData();
        } catch (e) {
            alert("保存失败"); // 中文提示
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center bg-white p-4 rounded-xl shadow-sm border border-blue-100">
                <h2 className="text-2xl font-bold text-slate-800 flex items-center gap-2">
                    <Settings className="w-6 h-6 text-blue-600" />
                    排放系数
                </h2>
                <button
                    onClick={() => setEditing({ coefficientType: '', coefficientValue: 0, remark: '' })}
                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg transition-colors"
                >
                    <Plus className="w-4 h-4" /> 新增
                </button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* 表格区域 */}
                <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-blue-50 overflow-hidden">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50 text-slate-600 font-semibold border-b border-slate-200">
                        <tr>
                            <th className="p-4">类型</th>
                            <th className="p-4">系数值</th>
                            <th className="p-4">说明</th>
                            <th className="p-4 text-right">操作</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100">
                        {coeffs.map(c => (
                            <tr key={c.id} className="hover:bg-blue-50/30 transition-colors">
                                <td className="p-4 font-medium text-blue-700">{c.coefficientType}</td>
                                <td className="p-4 font-bold text-slate-800">{c.coefficientValue}</td>
                                <td className="p-4 text-slate-500 text-sm">{c.remark}</td>
                                <td className="p-4 text-right">
                                    <button
                                        onClick={() => setEditing(c)}
                                        className="text-slate-400 hover:text-blue-600"
                                    >
                                        <Edit2 className="w-4 h-4" />
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {/* 图表区域 */}
                <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-50 flex flex-col">
                    <h3 className="text-lg font-bold text-slate-700 mb-4">系数对比</h3>
                    <div className="flex-1 min-h-[300px]">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={coeffs} layout="vertical" margin={{ left: 20 }}>
                                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                                <XAxis type="number" unit=" 千克CO₂" /> {/* 补充单位 */}
                                <YAxis dataKey="coefficientType" type="category" width={80} tick={{fontSize: 10}} />
                                <Tooltip
                                    cursor={{fill: '#f1f5f9'}}
                                    formatter={(value) => [`${value} 千克CO₂`, '系数值']} // 优化Tooltip提示
                                />
                                <Bar dataKey="coefficientValue" fill="#3b82f6" radius={[0, 4, 4, 0]} barSize={20} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* 编辑/新增弹窗 */}
            {editing && (
                <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-xl font-bold text-slate-800">
                                {editing.id ? '编辑系数' : '添加系数'}
                            </h3>
                            <button onClick={() => setEditing(null)} className="text-slate-400 hover:text-slate-600">
                                <X className="w-5 h-5" />
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-600 mb-1">类型标识</label>
                                <input
                                    type="text"
                                    disabled={!!editing.id}
                                    className="w-full border border-slate-300 rounded-lg p-2 disabled:bg-slate-100"
                                    value={editing.coefficientType}
                                    onChange={e => setEditing({...editing, coefficientType: e.target.value})}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-600 mb-1">数值</label>
                                <input
                                    type="number"
                                    className="w-full border border-slate-300 rounded-lg p-2"
                                    value={editing.coefficientValue}
                                    onChange={e => setEditing({...editing, coefficientValue: Number(e.target.value)})}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-slate-600 mb-1">说明</label>
                                <input
                                    type="text"
                                    className="w-full border border-slate-300 rounded-lg p-2"
                                    value={editing.remark}
                                    onChange={e => setEditing({...editing, remark: e.target.value})}
                                />
                            </div>

                            <div className="pt-4 flex justify-end gap-3">
                                <button
                                    onClick={() => setEditing(null)}
                                    className="px-4 py-2 text-slate-600 hover:bg-slate-50 rounded-lg"
                                >
                                    取消
                                </button>
                                <button
                                    onClick={handleSave}
                                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
                                >
                                    <Save className="w-4 h-4" /> 保存修改
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Coefficients;