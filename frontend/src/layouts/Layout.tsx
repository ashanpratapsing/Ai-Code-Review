import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, FolderKanban, Terminal, LogOut, ChevronRight, Activity, History as HistoryIcon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { cn, Button } from '../components/ui/core';

const SidebarItem = ({ to, icon: Icon, label }: { to: string; icon: any; label: string }) => (
  <NavLink
    to={to}
    className={({ isActive }) =>
      cn(
        'flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 group',
        isActive ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:text-foreground hover:bg-white/5'
      )
    }
  >
    <Icon className="w-5 h-5" />
    <span className="font-medium">{label}</span>
    <ChevronRight className="w-4 h-4 ml-auto opacity-0 group-hover:opacity-100 transition-opacity" />
  </NavLink>
);

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 border-r border-border bg-card/30 backdrop-blur-xl flex flex-col">
        <div className="p-6 flex items-center gap-3">
          <div className="w-10 h-10 bg-primary/20 rounded-xl flex items-center justify-center border border-primary/30">
            <Terminal className="text-primary w-6 h-6" />
          </div>
          <h1 className="text-xl font-bold tracking-tight">AI Review</h1>
        </div>

        <nav className="flex-1 px-4 space-y-2 mt-4">
          <SidebarItem to="/dashboard" icon={LayoutDashboard} label="Dashboard" />
          <SidebarItem to="/projects" icon={FolderKanban} label="Projects" />
          <SidebarItem to="/review" icon={Terminal} label="Code Review" />
          <SidebarItem to="/history" icon={HistoryIcon} label="History" />
        </nav>

        <div className="p-4 mt-auto">
          <div className="glass p-4 rounded-xl mb-4 border border-white/5">
            <div className="flex items-center gap-3 mb-2">
              <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-primary to-purple-500" />
              <div className="overflow-hidden">
                <p className="text-sm font-medium truncate">{user?.name || 'User'}</p>
                <p className="text-xs text-muted-foreground truncate">{user?.email}</p>
              </div>
            </div>
            <Button variant="ghost" size="sm" className="w-full justify-start text-muted-foreground" onClick={handleLogout}>
              <LogOut className="w-4 h-4 mr-2" />
              Sign Out
            </Button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary/5 via-transparent to-transparent">
        <header className="h-16 border-b border-border flex items-center justify-between px-8 backdrop-blur-md sticky top-0 z-10 bg-background/50">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
             <span>Space</span>
             <ChevronRight className="w-4 h-4" />
             <span className="text-foreground font-medium capitalize">{window.location.pathname.split('/')[1] || 'Home'}</span>
          </div>
          <div className="flex items-center gap-4">
             <Button variant="outline" size="sm">
                <Activity className="w-4 h-4 mr-2" />
                Documentation
             </Button>
          </div>
        </header>
        <div className="p-8">
          {children}
        </div>
      </main>
    </div>
  );
};
