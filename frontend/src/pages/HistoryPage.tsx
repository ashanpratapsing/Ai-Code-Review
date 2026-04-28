import React from 'react';
import { useQuery } from '@tanstack/react-query';
import axios from 'axios';
import { format } from 'date-fns';
import { 
  History as HistoryIcon, 
  Calendar, 
  Code, 
  BarChart3, 
  ChevronRight,
  Search,
  AlertCircle
} from 'lucide-react';
import { Card, Badge, Button } from '../components/ui/core';

interface HistoryItem {
  id: number;
  codeSnippet: string;
  resultJson: string;
  score: number;
  createdAt: string;
}

export const HistoryPage: React.FC = () => {
  const { data: history, isLoading, error } = useQuery<HistoryItem[]>({
    queryKey: ['analysis-history'],
    queryFn: async () => {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8080/history', {
        headers: { Authorization: `Bearer ${token}` }
      });
      return response.data;
    }
  });

  if (isLoading) {
    return (
      <div className="flex h-[80vh] items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-[80vh] flex-col items-center justify-center space-y-4">
        <AlertCircle className="h-16 w-16 text-destructive" />
        <h2 className="text-xl font-bold">Failed to load history</h2>
        <p className="text-muted-foreground">Please try again later or check your connection.</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 max-w-6xl mx-auto p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-3">
            <HistoryIcon className="h-8 w-8 text-primary" />
            Analysis History
          </h1>
          <p className="text-muted-foreground mt-2">View and manage your previous code reviews</p>
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input 
            type="text" 
            placeholder="Search history..." 
            className="pl-10 pr-4 py-2 rounded-full bg-secondary/20 border border-white/10 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all w-64"
          />
        </div>
      </div>

      <div className="grid gap-4">
        {history?.length === 0 ? (
          <div className="text-center py-20 bg-secondary/5 rounded-3xl border-2 border-dashed border-white/5">
            <HistoryIcon className="h-12 w-12 mx-auto text-muted-foreground mb-4 opacity-20" />
            <p className="text-muted-foreground">No analysis history found. Start your first review!</p>
          </div>
        ) : (
          history?.map((item) => (
            <Card key={item.id} className="group hover:bg-secondary/20 transition-all cursor-pointer border-white/5">
              <div className="p-5 flex items-center justify-between">
                <div className="flex items-center gap-6">
                  <div className="h-12 w-12 rounded-xl bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                    <Code className="h-6 w-6 text-primary" />
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-lg">Analysis #{item.id}</span>
                      <Badge variant={item.score > 7 ? 'success' : item.score > 4 ? 'warning' : 'error'}>
                         Score: {item.score}/10
                      </Badge>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="h-3.5 w-3.5" />
                        {format(new Date(item.createdAt), 'MMM dd, yyyy HH:mm')}
                      </div>
                      <div className="flex items-center gap-1.5">
                        <BarChart3 className="h-3.5 w-3.5" />
                        {item.codeSnippet.length} chars
                      </div>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <Button variant="ghost" className="opacity-0 group-hover:opacity-100 transition-opacity">
                    View Details
                  </Button>
                  <ChevronRight className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-colors" />
                </div>
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};
