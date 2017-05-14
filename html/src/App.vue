<template>
    <div id="app">
        <!-- Topbar -->
        <topbar></topbar>

        <!-- Sidebar -->
        <sidebar></sidebar>

        <!-- Main area -->
        <div class="main">
            <!-- Card dashboard -->
            <deck-dashboard :card-types="cardTypes"></deck-dashboard>
        </div>
    </div>
</template>

<script>
    import Vue from 'vue';
    import EventBus from './EventBus';
    import Sidebar from './components/Sidebar.vue';
    import DeckDashboard from './components/DeckDashboard.vue';
    import Topbar from './components/Topbar.vue';

    export default {
        name: 'app',
        components: {
            Topbar,
            Sidebar,
            DeckDashboard
        },
        data () {
            return {
                cardTypes: null,
                decks: null
            }
        },

        mounted () {
            // Download decks
            Vue.http.get('/decks.json')
                .then(response => response.json())
                .then(data => {
                    this.decks = data.decks;
                    this.cardTypes = data.cardTypes;

                    // Emit the download completed event
                    EventBus.$emit('downloadCompleted', data);
                })
                .catch(error => {
                    console.error(error);
                    alert("Non riesco a caricare le carte ...");
                });
        }
    }
</script>

<style lang="scss" type="text/scss">

    @import "style-settings.scss";

    body {
        margin: 0;
    }

    * {
        font-family: 'Roboto', "Lucida Sans Unicode", "Lucida Grande", Sans-Serif, serif;
    }

    .main {
        width: 100%;
    }

    @media (min-width: 480px) {
        .main {
            margin-left: $sidebarWidth;
            width: calc(100% - #{$sidebarWidth});
        }
    }
</style>