import Vue from 'vue'
import VueResource from 'vue-resource';

import App from './App.vue'; // Main app component

// Configure vue
Vue.use(VueResource);



// Start the vue app
new Vue({
    el: '#app',
    render: h => h(App)
});