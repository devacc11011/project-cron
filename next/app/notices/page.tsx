import NoticeList from '@/components/notice-list';
import { Navbar } from '@/components/navbar';

export default function NoticesPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 dark:from-slate-950 dark:to-blue-950">
      <Navbar />
      <div className="pt-8">
        <NoticeList />
      </div>
    </div>
  );
}
