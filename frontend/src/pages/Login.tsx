import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import api from '../services/api';
import { Loader2 } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const loginMutation = useMutation({
    mutationFn: async () => {
      const res = await api.post('/auth/login', { email, password });
      return res.data;
    },
    onSuccess: (data) => {
      login(data.token);
      toast('Welcome back!', 'success');
      navigate('/');
    },
    onError: () => {
      toast('Invalid credentials. Please try again.', 'error');
    }
  });

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0d1117] text-white">
      <div className="bg-[#161b22] border border-gray-800 p-8 rounded-xl w-[400px] shadow-2xl">
        <h2 className="text-2xl font-bold mb-6 text-center">AI Code Review</h2>
        <form onSubmit={(e) => { e.preventDefault(); loginMutation.mutate(); }} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Email</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-[#0d1117] border border-gray-800 rounded-md px-3 py-2 outline-none focus:border-blue-500" 
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Password</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-[#0d1117] border border-gray-800 rounded-md px-3 py-2 outline-none focus:border-blue-500"
              required 
            />
          </div>
          <button 
            disabled={loginMutation.isPending}
            type="submit" 
            className="w-full bg-blue-600 hover:bg-blue-500 py-2 rounded-md font-medium transition-colors flex justify-center mt-4">
              {loginMutation.isPending ? <Loader2 className="animate-spin w-5 h-5" /> : 'Log In'}
          </button>
        </form>
      </div>
    </div>
  );
}
