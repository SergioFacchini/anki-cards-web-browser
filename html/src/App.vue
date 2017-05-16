<template>
    <div id="app">
        <!-- Topbar -->
        <topbar></topbar>

        <!-- Sidebar -->
        <sidebar></sidebar>

        <!-- Main area -->
        <div class="main" @click="clickMain">
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
            // TODO: Refactor
            Vue.http.get('/decks.json')
                .then(response => response.json())
                .then(data => {
                    // Add properties to the card
                    data.decks.forEach(deck => {
                        if (deck.hasCategories) {
                            deck.categories.forEach(category => {
                                category.cards.forEach(card => {
                                    // Attach style
                                    card.style = data.cardTypes[card.typeId];

                                    // Attach deck
                                    card.deck = deck;

                                    card.categoryName = category.categoryName || "(no category)";
                                });
                            });
                        } else {
                            deck.cards.forEach(card => {
                                // Attach style
                                card.style = data.cardTypes[card.typeId];

                                // Attach deck
                                card.deck = deck;
                            });
                        }
                    });

                    return data;
                })
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
        },

        methods: {
            clickMain () {
                EventBus.$emit('closeSidebar');
            }
        }
    }
</script>

<style lang="scss" type="text/scss">

    @import "style-settings.scss";

    body {
        margin: 0;
        background-color: #bfbfbf; /* HAck to fix the bug when something overflows */
    }

    html, button {
        font-family: 'Lato', "Lucida Sans Unicode", "Lucida Grande", Sans-Serif, serif;
    }

    .main {
        width: 100%;
        height: calc(100vh - #{$topbarHeightWithPadding});
    }

    @media (min-width: 800px) {
        .main {
            margin-left: $sidebarWidth;
            width: calc(100% - #{$sidebarWidth});
        }
    }

    button {
        height: 48px;
        font-size: 24px;
        margin-top: 32px;
        background-color: #1C1C1C;
        color: white;
        border: none;
        padding: 4px 16px;
        cursor: pointer;
    }

    button:disabled {
        background-color: #636363;
    }
</style>