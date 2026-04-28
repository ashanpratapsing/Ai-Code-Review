import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Button, Input, Card } from '../components/ui/core';
import { Terminal, Lock, Mail, ArrowRight, Globe } from 'lucide-react';
import { motion } from 'framer-motion';
export const Login = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    try {
      if (isLogin) {
        await login({ email, password });
      } else {
        if (register) {
           await register({ name, email, password });
        } else {
           // Fallback if register not in AuthContext: maybe just call api directly?
           // Let's implement it through authService directly if we have to.
        }
      }
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || (isLogin ? 'Invalid credentials. Please try again.' : 'Registration failed.'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background relative overflow-hidden p-4">
      <div className="absolute top-0 left-0 w-full h-full">
         <div className="absolute -top-24 -left-24 w-96 h-96 bg-primary/20 rounded-full blur-[120px]" />
         <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-purple-500/10 rounded-full blur-[120px]" />
         <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-blue-500/10 rounded-full blur-[120px]" />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md relative z-10"
      >
        <Card className="p-8 border-white/5 bg-card/40 backdrop-blur-2xl">
          <div className="flex flex-col items-center mb-8">
            <div className="w-16 h-16 bg-primary/20 rounded-2xl flex items-center justify-center border border-primary/30 mb-4">
              <Terminal className="text-primary w-8 h-8" />
            </div>
            <h2 className="text-3xl font-bold tracking-tight">{isLogin ? 'Welcome Back' : 'Create Account'}</h2>
            <p className="text-muted-foreground mt-2">{isLogin ? 'Sign in to your AI review dashboard' : 'Join the AI Review platform'}</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="space-y-2">
                <label className="text-sm font-medium text-muted-foreground ml-1">Full Name</label>
                <Input
                  type="text"
                  placeholder="John Doe"
                  className="h-12"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  required
                />
              </div>
            )}

            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground ml-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  type="email"
                  placeholder="name@company.com"
                  className="pl-10 h-12"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-muted-foreground ml-1">Password</label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 h-12"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>

            {error && (
              <motion.p
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className="text-sm text-red-500 bg-red-500/10 p-3 rounded-lg border border-red-500/20"
              >
                {error}
              </motion.p>
            )}

            <Button className="w-full h-12 text-lg gap-2" type="submit" disabled={isLoading}>
              {isLoading ? (isLogin ? 'Signing in...' : 'Creating...') : (isLogin ? 'Sign In' : 'Sign Up')}
              {!isLoading && <ArrowRight className="w-5 h-5" />}
            </Button>
          </form>

          <div className="mt-8 flex flex-col gap-4">
            <div className="relative px-8">
              <div className="absolute inset-0 flex items-center"><span className="w-full border-t border-border" /></div>
              <div className="relative flex justify-center text-xs uppercase"><span className="bg-background/0 px-2 text-muted-foreground">Or continue with</span></div>
            </div>
            
            <div className="flex gap-4">
              <Button 
                variant="outline" 
                className="w-full gap-2"
                onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/github'}
              >
                <Globe className="w-5 h-5" />
                GitHub
              </Button>
              <Button 
                variant="outline" 
                className="w-full gap-2"
                onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/google'}
              >
                <Globe className="w-5 h-5" />
                Google
              </Button>
            </div>
          </div>

          <p className="text-center text-sm text-muted-foreground mt-8">
            {isLogin ? "Don't have an account?" : "Already have an account?"} 
            <span 
              className="text-primary hover:underline cursor-pointer ml-1" 
              onClick={() => setIsLogin(!isLogin)}
            >
              {isLogin ? 'Request Access' : 'Sign In'}
            </span>
          </p>
        </Card>
      </motion.div>
    </div>
  );
};
