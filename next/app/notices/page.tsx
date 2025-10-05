import NoticeList from '@/components/notice-list';
import { Navbar } from '@/components/navbar';

export default function NoticesPage() {
  return (
    <div className="min-h-screen bg-black">
      <Navbar />
      <div className="absolute inset-0 bg-gradient-to-br from-blue-500/5 via-purple-500/5 to-pink-500/5 pointer-events-none"></div>
      <div className="relative pt-8">
        <NoticeList />
      </div>
    </div>
  );
}
