import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../services/api';
import { FolderPlus, Loader2, Search, MoreVertical, Plus } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { useToast } from '../context/ToastContext';

export default function Projects() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [showModal, setShowModal] = useState(false);
  const [pname, setPname] = useState('');
  const [desc, setDesc] = useState('');

  const { data: projects, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      const res = await api.get('/projects');
      return res.data;
    }
  });

  const createMutation = useMutation({
    mutationFn: async () => {
      const res = await api.post('/projects', { name: pname, description: desc });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast('Project created successfully', 'success');
      setShowModal(false);
      setPname('');
      setDesc('');
    },
    onError: () => {
      toast('Failed to create project', 'error');
    }
  });

  return (
    <div className="p-8 bg-[#0d1117] min-h-screen text-white">
      <div className="max-w-7xl mx-auto text-left">
        <div className="flex justify-between items-center mb-10">
          <div>
            <h1 className="text-3xl font-bold">Projects</h1>
            <p className="text-gray-400 mt-2">Manage your code repositories and review history.</p>
          </div>
          <button 
            onClick={() => setShowModal(true)}
            className="bg-blue-600 hover:bg-blue-500 text-white px-5 py-2.5 rounded-lg flex items-center gap-2 font-medium transition-all"
          >
            <Plus className="w-5 h-5" /> New Project
          </button>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center p-20">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {projects?.map((proj: any, i: number) => (
              <motion.div 
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.05 }}
                key={proj.id}
                className="bg-[#161b22] border border-[#30363d] p-6 rounded-xl hover:border-blue-500/50 transition-all cursor-pointer group shadow-lg"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="p-2.5 bg-blue-600/10 rounded-lg text-blue-500">
                    <FolderPlus className="w-6 h-6" />
                  </div>
                  <button className="text-gray-500 hover:text-white p-1">
                    <MoreVertical className="w-5 h-5" />
                  </button>
                </div>
                <h3 className="text-lg font-bold mb-2 group-hover:text-blue-400 transition-colors uppercase tracking-tight italic">{proj.name}</h3>
                <p className="text-gray-400 text-sm line-clamp-2 h-10">{proj.description || 'No description provided for this project.'}</p>
                <div className="mt-6 pt-6 border-t border-[#30363d] flex justify-between items-center">
                  <span className="text-xs text-gray-500 font-medium">Status: Active</span>
                  <span className="text-blue-500 text-sm font-bold">Open Project →</span>
                </div>
              </motion.div>
            ))}
          </div>
        )}

        {/* Modal */}
        <AnimatePresence>
          {showModal && (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
              <motion.div 
                initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setShowModal(false)} 
              />
              <motion.div 
                initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }}
                className="bg-[#161b22] border border-[#30363d] p-8 rounded-2xl w-full max-w-md relative z-10 shadow-2xl"
              >
                <h2 className="text-2xl font-bold mb-6 italic text-blue-400 uppercase tracking-tighter">Create Project</h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-400 mb-1">Project Name</label>
                    <input 
                      className="w-full bg-[#0d1117] border border-[#30363d] rounded-lg px-4 py-3 outline-none focus:border-blue-500 transition-colors"
                      placeholder="e.g. Microservices-Backend"
                      value={pname} onChange={e => setPname(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-400 mb-1">Description</label>
                    <textarea 
                      className="w-full bg-[#0d1117] border border-[#30363d] rounded-lg px-4 py-3 outline-none focus:border-blue-500 transition-colors h-24"
                      placeholder="Short summary of this project..."
                      value={desc} onChange={e => setDesc(e.target.value)}
                    />
                  </div>
                  <div className="flex gap-4 pt-4">
                    <button 
                      className="flex-1 bg-gray-800 hover:bg-gray-700 py-3 rounded-lg font-bold"
                      onClick={() => setShowModal(false)}
                    >Cancel</button>
                    <button 
                      className="flex-1 bg-blue-600 hover:bg-blue-500 py-3 rounded-lg font-bold flex justify-center disabled:opacity-50"
                      onClick={() => createMutation.mutate()}
                      disabled={createMutation.isPending}
                    >
                      {createMutation.isPending ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Create'}
                    </button>
                  </div>
                </div>
              </motion.div>
            </div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
