import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService } from '../services/api';
import { Card, Button, Input, Badge } from '../components/ui/core';
import { FolderPlus, Search, Trash2, Code2, Calendar, MoreVertical } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export const Projects = () => {
  const [search, setSearch] = useState('');
  const queryClient = useQueryClient();

  const { data: projects, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
       const resp = await projectService.getProjects();
       return resp.data;
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => projectService.deleteProject(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['projects'] }),
  });

  const filteredProjects = projects?.filter(p => 
    p.name.toLowerCase().includes(search.toLowerCase()) || 
    p.language.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-end">
        <div>
          <h2 className="text-3xl font-bold tracking-tight mb-2">Projects</h2>
          <p className="text-muted-foreground">Manage your code repositories and review history.</p>
        </div>
        <Button className="gap-2">
          <FolderPlus className="w-4 h-4" />
          New Project
        </Button>
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input 
          placeholder="Filter projects by name or language..." 
          className="pl-10 max-w-md"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <AnimatePresence>
          {isLoading ? (
            [1, 2, 3].map(i => (
              <div key={i} className="h-48 glass rounded-xl animate-pulse" />
            ))
          ) : (
            filteredProjects?.map((project, index) => (
              <motion.div
                key={project.id}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ delay: index * 0.05 }}
              >
                <Card className="h-full flex flex-col group hover:border-primary/50 transition-colors">
                  <div className="flex justify-between items-start mb-4">
                    <div className="w-12 h-12 bg-secondary rounded-lg flex items-center justify-center">
                      <Code2 className="text-primary w-6 h-6" />
                    </div>
                    <Button variant="ghost" size="icon" className="text-muted-foreground">
                      <MoreVertical className="w-4 h-4" />
                    </Button>
                  </div>

                  <h4 className="text-lg font-bold mb-1">{project.name}</h4>
                  <p className="text-sm text-muted-foreground line-clamp-2 mb-4 flex-1">
                    {project.description || 'No description provided for this project.'}
                  </p>

                  <div className="flex items-center gap-4 text-xs text-muted-foreground mb-4">
                    <div className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5" />
                      {new Date(project.updatedAt).toLocaleDateString()}
                    </div>
                    <Badge variant="outline" className="capitalize">{project.language}</Badge>
                  </div>

                  <div className="flex gap-2">
                    <Button variant="secondary" size="sm" className="flex-1" onClick={() => window.location.href = `/review?projectId=${project.id}`}>
                      View Files
                    </Button>
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="text-destructive hover:bg-destructive/10"
                      onClick={() => deleteMutation.mutate(project.id)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </Card>
              </motion.div>
            ))
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};
