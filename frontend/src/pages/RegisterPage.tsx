import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { Lock, Mail, User } from 'lucide-react';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import api from '../lib/api';
import { useAuthStore } from '../store/authStore';
import type { RegisterFormData, AuthResponse } from '../types';

export default function RegisterPage() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>();
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const setAuth = useAuthStore((state) => state.setAuth);

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    try {
      const res = await api.post<AuthResponse>('/api/auth/register', data);
      setAuth(res.data.token, res.data.username);
      navigate('/');
    } catch {
      alert('회원가입에 실패했습니다. 이미 사용 중인 이메일일 수 있습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className='min-h-screen flex items-center justify-center p-4 bg-background'>
      <div className='w-full max-w-md space-y-8'>
        <div className='text-center space-y-2'>
          <h1 className='text-3xl font-bold bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent'>
            회원가입
          </h1>
          <p className='text-slate-400'>효율적인 업무 관리를 시작하세요</p>
        </div>

        <div className='card p-8 space-y-6'>
          <form onSubmit={handleSubmit(onSubmit)} className='space-y-4'>
            <div className='relative'>
              <Input
                label='사용자명'
                {...register('name', { required: '사용자명을 입력해주세요' })}
                error={errors.name?.message as string}
                placeholder='사용자명을 입력하세요'
                className='pl-10'
              />
              <User className='absolute left-3 top-[34px] w-5 h-5 text-slate-500' />
            </div>

            <div className='relative'>
              <Input
                label='이메일'
                type='email'
                {...register('email', {
                  required: '이메일을 입력해주세요',
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: '올바른 이메일 형식이 아닙니다',
                  },
                })}
                error={errors.email?.message as string}
                placeholder='이메일을 입력하세요'
                className='pl-10'
              />
              <Mail className='absolute left-3 top-[34px] w-5 h-5 text-slate-500' />
            </div>

            <div className='relative'>
              <Input
                type='password'
                label='비밀번호'
                {...register('password', {
                  required: '비밀번호를 입력해주세요',
                  minLength: {
                    value: 6,
                    message: '최소 6자 이상이어야 합니다',
                  },
                })}
                error={errors.password?.message as string}
                placeholder='비밀번호를 입력하세요'
                className='pl-10'
              />
              <Lock className='absolute left-3 top-[34px] w-5 h-5 text-slate-500' />
            </div>

            <Button
              type='submit'
              className='w-full h-12 text-lg'
              isLoading={isLoading}
            >
              가입하기
            </Button>
          </form>

          <div className='text-center text-sm text-slate-400'>
            이미 계정이 있으신가요?{' '}
            <Link
              to='/login'
              className='text-primary hover:text-blue-400 font-medium transition-colors'
            >
              로그인
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
