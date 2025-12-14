import type { InputHTMLAttributes } from 'react';
import { forwardRef } from 'react';
import { cn } from '../../lib/utils';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, ...props }, ref) => {
    return (
      <div className='w-full space-y-1.5'>
        {label && (
          <label className='text-sm font-medium text-slate-300 ml-1'>
            {label}
          </label>
        )}
        <input
          ref={ref}
          className={cn(
            'w-full px-4 py-2.5 bg-slate-900/50 border border-slate-700 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all placeholder-slate-500 text-white',
            error &&
              'border-red-500 focus:ring-red-500/50 focus:border-red-500',
            className
          )}
          {...props}
        />
        {error && <p className='text-sm text-red-500 ml-1'>{error}</p>}
      </div>
    );
  }
);

Input.displayName = 'Input';
export default Input;
