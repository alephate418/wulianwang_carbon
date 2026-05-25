import React, { useState } from 'react';
import { CarbonService } from '../services/api';
import type { CarbonData } from '../types';
import { Calculator as CalcIcon, Zap, Flame, Trees, ArrowRight } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

// 新增：类型映射函数（将英文类型转为中文）
const getTypeCN = (type: string) => {
    switch (type) {
        case 'ELECTRICITY':
            return '用电';
        case 'GAS':
            return '天然气';
        default:
            return type;
    }
};

const Calculator: React.FC = () => {
    const [inputs, setInputs] = useState({
        ELECTRICITY: 0,
        GAS: 0,
        TREE: 0
    });

    const [result, setResult] = useState<CarbonData | null>(null);
    // 假设areaId有一个默认值，也可以根据实际情况从props或其他地方获取
    const areaId = 1; // 这里根据实际业务场景设置合适的初始值

    const handleCalc = async () => {
        try {
            // 修复了拼写错误 areald -> areaId
            const res = await CarbonService.calculate(areaId, inputs);
            setResult(res);
        } catch { // 移除了未使用的e变量
            // Mock result
            setResult({
                id: 999,
                areaId: 1,
                totalCarbon: (inputs.ELECTRICITY * 0.58) + (inputs.GAS * 2.1),
                sequestration: inputs.TREE * 0.05,
                netCarbon: ((inputs.ELECTRICITY * 0.58) + (inputs.GAS * 2.1)) - (inputs.TREE * 0.05),
                collectTime: new Date().toISOString(),
                createTime: new Date().toISOString(),
                emissionConsumes: [
                    { id: null, cdId: 999, coefficientType: 'ELECTRICITY', consumeAmount: inputs.ELECTRICITY, carbonEmission: inputs.ELECTRICITY * 0.58, createTime: '', updateTime: '' },
                    { id: null, cdId: 999, coefficientType: 'GAS', consumeAmount: inputs.GAS, carbonEmission: inputs.GAS * 2.1, createTime: '', updateTime: '' }
                ]
            });
        }
    };

    return (
        <div className="space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm border border-blue-100">
                <h2 className="text-2xl font-bold text-slate-800 flex items-center gap-2 mb-6">
                    <CalcIcon className="w-6 h-6 text-blue-600" />
                    碳排放量模拟器
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                    {/* Input Form */}
                    <div className="space-y-6">
                        <h3 className="font-semibold text-slate-600 border-b pb-2">输入消耗数据</h3>

                        <div className="space-y-4">
                            <div>
                                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-1">
                                    <Zap className="w-4 h-4 text-yellow-500" /> 用电量 (千瓦时)
                                </label>
                                <input
                                    type="number"
                                    className="w-full border border-slate-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-400 outline-none"
                                    value={inputs.ELECTRICITY}
                                    onChange={e => setInputs({...inputs, ELECTRICITY: Number(e.target.value)})}
                                />
                            </div>

                            <div>
                                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-1">
                                    <Flame className="w-4 h-4 text-orange-500" /> 天然气用量 (立方米)
                                </label>
                                <input
                                    type="number"
                                    className="w-full border border-slate-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-400 outline-none"
                                    value={inputs.GAS}
                                    onChange={e => setInputs({...inputs, GAS: Number(e.target.value)})}
                                />
                            </div>

                            <div>
                                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-1">
                                    <Trees className="w-4 h-4 text-green-600" /> 树木固碳量 (棵)
                                </label>
                                <input
                                    type="number"
                                    className="w-full border border-slate-300 rounded-lg p-3 focus:ring-2 focus:ring-blue-400 outline-none"
                                    value={inputs.TREE}
                                    onChange={e => setInputs({...inputs, TREE: Number(e.target.value)})}
                                />
                            </div>

                            <button
                                onClick={handleCalc}
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 rounded-lg shadow-md transition-all flex justify-center items-center gap-2 mt-4"
                            >
                                计算排放量 <ArrowRight className="w-4 h-4" />
                            </button>
                        </div>
                    </div>

                    {/* Results Area */}
                    <div className="md:col-span-2 bg-slate-50 rounded-xl p-6 border border-slate-200">
                        {result ? (
                            <div className="h-full flex flex-col">
                                <div className="grid grid-cols-3 gap-4 mb-6">
                                    <div className="bg-white p-4 rounded-lg shadow-sm text-center">
                                        <span className="block text-xs text-slate-500 uppercase">总排放量</span>
                                        <span className="text-xl font-bold text-blue-600">{result.totalCarbon.toFixed(1)}</span>
                                        <span className="block text-xs text-slate-400">kgCO₂</span>
                                    </div>
                                    <div className="bg-white p-4 rounded-lg shadow-sm text-center">
                                        <span className="block text-xs text-slate-500 uppercase">固碳量</span>
                                        <span className="text-xl font-bold text-green-600">{result.sequestration.toFixed(1)}</span>
                                        <span className="block text-xs text-slate-400">kgCO₂</span>
                                    </div>
                                    <div className="bg-white p-4 rounded-lg shadow-sm text-center ring-2 ring-indigo-100">
                                        <span className="block text-xs text-slate-500 uppercase">净排放量</span>
                                        <span className="text-2xl font-bold text-indigo-600">{result.netCarbon.toFixed(1)}</span>
                                        <span className="block text-xs text-slate-400">kgCO₂</span>
                                    </div>
                                </div>

                                <div className="flex-1 bg-white p-4 rounded-lg shadow-sm">
                                    <h4 className="text-sm font-semibold text-slate-600 mb-4">各来源排放量占比</h4>
                                    <ResponsiveContainer width="100%" height="100%" minHeight={200}>
                                        <BarChart data={result.emissionConsumes} layout="vertical">
                                            <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                                            <XAxis type="number" unit=" kgCO₂" />
                                            <YAxis
                                                dataKey="coefficientType"
                                                type="category"
                                                width={80}
                                                tick={{fontSize: 10}}
                                                tickFormatter={getTypeCN} // 使用映射函数显示中文
                                            />
                                            <Tooltip
                                                formatter={(value) => [`${value} kgCO₂`, '排放量']}
                                                labelFormatter={getTypeCN}
                                            />
                                            <Legend />
                                            <Bar dataKey="carbonEmission" fill="#3b82f6" name="排放量 (千克二氧化碳)" barSize={20} radius={[0, 4, 4, 0]} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        ) : (
                            <div className="h-full flex items-center justify-center text-slate-400 flex-col gap-4">
                                <CalcIcon className="w-16 h-16 opacity-20" />
                                <p>输入消耗数据以计算环境影响。</p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Calculator;