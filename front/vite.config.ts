import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    base: '/alnet/',
    plugins: [vue()],
    server: {
        port: 3000,
        host: '0.0.0.0',
        allowedHosts: [
            'platovd.ru'
        ]
    }
});


