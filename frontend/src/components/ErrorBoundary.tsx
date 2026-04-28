import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false
  };

  public static getDerivedStateFromError(_: Error): State {
    return { hasError: true };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-[#0d1117] flex items-center justify-center p-8 text-center">
          <div className="max-w-md bg-[#161b22] border border-red-900/50 p-10 rounded-2xl shadow-2xl">
            <h1 className="text-2xl font-bold text-red-500 mb-4 uppercase italic">Critical System Failure</h1>
            <p className="text-gray-400 mb-8">The application encountered an unexpected error. Please refresh the browser or contact system admin.</p>
            <button 
              onClick={() => window.location.reload()}
              className="bg-red-600 hover:bg-red-500 text-white px-8 py-3 rounded-xl font-bold transition-all"
            >
              Force Restart
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
