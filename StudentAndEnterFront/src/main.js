//import './assets/main.css'

// import { createApp } from 'vue'
// import App from './App.vue'
// import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'
// import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//
// const app = createApp(App)
// for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
//     app.component(key, component)
// }
// app.use(ElementPlus)
//
// createApp(App).mount('#app')


import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import './assets/portfolio.css';
import './assets/workspace.css';
import './assets/module-pages.css';

// Import global components
import StarDisplay from './components/Common/StarDisplay.vue';
import RatingDialog from './components/Common/RatingDialog.vue';

const app = createApp(App);

// Register global components
app.component('StarDisplay', StarDisplay);
app.component('RatingDialog', RatingDialog);

app.use(router);
app.use(ElementPlus);
app.mount('#app');