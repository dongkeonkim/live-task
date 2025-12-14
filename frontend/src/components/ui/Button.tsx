import type { ButtonHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { cn } from '../../lib/utils';
import { Loader2 } from 'lucide-react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  isLoading?: boolean;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', isLoading, children, ...props }, ref) => {
    const variants = {
      primary:
        'bg-primary hover:bg-blue-600 text-white shadow-lg shadow-blue-500/25',
      secondary:
        'bg-surface hover:bg-slate-700 text-white border border-slate-700',
      ghost:
        'bg-transparent hover:bg-slate-800 text-slate-300 hover:text-white',
    };

    return (
      <button
        ref={ref}
        className={cn(
          'relative flex items-center justify-center px-4 py-2.5 rounded-lg font-medium transition-all duration-200 active:scale-95 disabled:opacity-50 disabled:pointer-events-none',
          variants[variant],
          className
        )}
        disabled={isLoading || props.disabled}
        {...props}
      >
        {isLoading && <Loader2 className='absolute w-4 h-4 animate-spin' />}
        <span
          className={cn('flex items-center gap-2', isLoading && 'invisible')}
        >
          {children}
        </span>
      </button>
    );
  }
);

Button.displayName = 'Button';
export default Button;
