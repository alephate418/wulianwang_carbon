import React, { useState } from 'react';
import { HashRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import {
    LayoutDashboard,
    BarChart2,
    AlertCircle,
    Settings2,
    Map,
    Calculator,
    Menu,
} from 'lucide-react';
import Dashboard from './components/Dashboard';
import Statistics from './components/Statistics';
import Warnings from './components/Warnings';
import Coefficients from './components/Coefficients';
import Areas from './components/Areas';
import CalculatorComponent from './components/Calculator';

const NavItem: React.FC<{ to: string; icon: React.ReactNode; label: string; onClick?: () => void }> = ({ to, icon, label, onClick }) => {
    const location = useLocation();
    const isActive = location.pathname === to;

    return (
        <Link
            to={to}
            onClick={onClick}
            className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium ${
                isActive
                    ? 'bg-blue-600 text-white shadow-lg shadow-blue-200'
                    : 'text-slate-500 hover:bg-blue-50 hover:text-blue-600'
            }`}
        >
            {icon}
            <span>{label}</span>
        </Link>
    );
};

const App: React.FC = () => {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    return (
        <Router>
            <div className="min-h-screen bg-slate-50 text-slate-800 flex font-sans">

                {/* Mobile Overlay */}
                {mobileMenuOpen && (
                    <div
                        className="fixed inset-0 bg-black/50 z-40 lg:hidden"
                        onClick={() => setMobileMenuOpen(false)}
                    />
                )}

                {/* Sidebar */}
                <aside className={`
          fixed lg:static inset-y-0 left-0 z-50 w-64 bg-white border-r border-slate-200 transform transition-transform duration-300 ease-in-out
          ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}>
                    <div className="h-full flex flex-col p-6">
                        <div className="flex items-center gap-3 mb-10 px-2">
                            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                                <span className="text-white font-bold text-lg">碳</span> {/* 替换原E为"碳"更贴合业务 */}
                            </div>
                            <h1 className="text-xl font-bold text-slate-800 tracking-tight">校园碳管理系统</h1>
                        </div>

                        <nav className="space-y-2 flex-1">
                            <NavItem to="/" icon={<LayoutDashboard className="w-5 h-5"/>} label="实时监控" onClick={() => setMobileMenuOpen(false)} />
                            <NavItem to="/stats" icon={<BarChart2 className="w-5 h-5"/>} label="趋势分析" onClick={() => setMobileMenuOpen(false)} />
                            <NavItem to="/warnings" icon={<AlertCircle className="w-5 h-5"/>} label="警告管理" onClick={() => setMobileMenuOpen(false)} />
                            <NavItem to="/areas" icon={<Map className="w-5 h-5"/>} label="校园区域" onClick={() => setMobileMenuOpen(false)} />
                            <NavItem to="/coefficients" icon={<Settings2 className="w-5 h-5"/>} label="排放系数" onClick={() => setMobileMenuOpen(false)} />
                            <NavItem to="/calculate" icon={<Calculator className="w-5 h-5"/>} label="碳排模拟器" onClick={() => setMobileMenuOpen(false)} />
                        </nav>

                        <div className="pt-6 border-t border-slate-100">
                            <div className="bg-blue-50 rounded-xl p-4">
                                <p className="text-xs text-blue-600 font-bold uppercase mb-1">系统状态</p>
                                <div className="flex items-center gap-2">
                                    <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                                    <span className="text-sm font-medium text-slate-700">在线</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </aside>

                {/* Main Content */}
                <main className="flex-1 flex flex-col min-h-screen overflow-hidden">
                    {/* Header */}
                    <header className="h-16 bg-white/80 backdrop-blur-md border-b border-slate-200 flex items-center justify-between px-6 lg:px-8 sticky top-0 z-30">
                        <button
                            className="lg:hidden p-2 -ml-2 text-slate-600"
                            onClick={() => setMobileMenuOpen(true)}
                        >
                            <Menu className="w-6 h-6" />
                        </button>
                        <div className="ml-auto flex items-center gap-4">
                            <div className="text-right hidden sm:block">
                                <p className="text-sm font-bold text-slate-700">管理员</p>
                                <p className="text-xs text-slate-400">校园管理员</p>
                            </div>
                            <div className="w-10 h-10 bg-slate-200 rounded-full overflow-hidden border-2 border-white shadow-sm">
                                <img src="https://picsum.photos/100" alt="头像" className="w-full h-full object-cover" /> {/* alt文本改为中文 */}
                            </div>
                        </div>
                    </header>

                    {/* Page Content Scrollable Area */}
                    <div className="flex-1 overflow-auto p-6 lg:p-8">
                        <div className="max-w-7xl mx-auto">
                            <Routes>
                                <Route path="/" element={<Dashboard />} />
                                <Route path="/stats" element={<Statistics />} />
                                <Route path="/warnings" element={<Warnings />} />
                                <Route path="/coefficients" element={<Coefficients />} />
                                <Route path="/areas" element={<Areas />} />
                                <Route path="/calculate" element={<CalculatorComponent />} />
                            </Routes>
                        </div>
                    </div>
                </main>
            </div>
        </Router>
    );
};

export default App;