<template>
    <div class="sidebar" ref="sidebar">

        <!-- Current session info -->
        <div class="session-info" v-if="session">
            <!-- TODO: Show time -->
            <!-- TODO: Show stop button -->
            <button class="button" @click="stopSession">Stop</button>
        </div>

        <!-- Container of the session choices -->
        <div class="session-setup" v-else="decks != null">

            <!-- Select deck -->
            <h3>Mazzo</h3>
            <div class="select-deck-container">
                <div>
                    <select class="select-deck" v-model="selectedDeck">
                        <option v-for="deck in decks" :value="deck">{{ deck.name }}</option>
                    </select>
                </div>
            </div>

            <!-- Category box -->
            <div v-if="selectedDeck.hasCategories">
                <h3>Categorie</h3>
                <div class="category-list-container">
                    <ul>
                        <li v-for="category in selectedDeck.categories">
                            <label><input type="checkbox" v-model="selectedCategories"
                                          :value="category.categoryName">{{ category.categoryName || "(no category)" }}</label>
                        </li>
                    </ul>
                </div>
            </div>

            <!-- Random toggle -->
            <div class="preferences-container">
                <label><input type="checkbox" v-model="randomizeCards">Mescola carte</label>
            </div>

            <!-- Start to study button -->
            <div>
                <button class="button" :disabled="!canStart" @click="start">Studia!</button>
            </div>
        </div>

        <!-- Show loading message -->
        <div class="loading-box" v-else>
            <p>Caricamento</p>
        </div>
    </div>
</template>
<script>
    import EventBus from '../EventBus';

    export default {

        data () {
            return {
                decks: null,

                // Flag that indicates if the session is running (card are shown in sequence)
                session: false,

                randomizeCards: false,
                selectedDeck: {},

                // Array of names of selected categories
                selectedCategories: []
            }
        },

        /**
         * Method called by vue when the component is attached to the DOM
         */
        mounted () {
            // Register the listener for the event that other components emit to toggle the sidebar
            EventBus.$on('toggleSidebar', () => this.toggleSidebar());

            EventBus.$on('downloadCompleted', (data) => {
                this.decks = data.decks;

                // Select the first deck
                this.selectedDeck = data.decks[0];
            });
        },

        computed: {
            /**
             * Computed value that checks if the session can be started
             * */
            canStart () {
                if (this.selectedDeck == null) {
                    return false;
                }

                if (!this.selectedDeck.hasCategories) {
                    return true;
                }

                // If the deck has a category, at least one must be selected
                return this.selectedCategories.length > 0;
            },
        },
        methods: {
            /**
             * Method to toggle the sidennav
             */
            toggleSidebar () {
                const classes = this.$refs.sidebar.classList;
                if (classes.contains('closed')) {
                    classes.remove('closed');
                } else {
                    classes.add('closed');
                }
            },

            /**
             * Close the sidenav if open
             */
            closeSidebar () {
                const classes = this.$refs.sidebar.classList;
                if (!classes.contains('closed')) {
                    classes.add('closed');
                }
            },

            /**
             * Start the session. This method emit the global event 'start' with an object that includes
             * the selected deck, the randomize flag and, if the dack has them, the array of categories (objects, not stirngs)
             */
            start () {
                // Create the object with the session preferences
                const deck = this.selectedDeck;
                const info = {
                    deck,
                    randomize: this.randomize
                };

                if (deck.hasCategories) {
                    // Create the array of all the selected categories
                    const categoryNames = this.selectedCategories;
                    info.selectedCategories = deck.categories.filter(category => categoryNames.indexOf(category.categoryName) >= 0);
                }

                // Close sidebar if opened and change its content
                this.closeSidebar();
                this.session = true;

                // Emit the global event with the array of cards
                EventBus.$emit('start', info);
            },

            stopSession () {
                this.session = false;
                EventBus.$emit('stop');
            }
        }
    }
</script>
<style lang="scss" scoped>

    @import "../style-settings.scss";

    .sidebar {
        position: absolute;
        z-index: 1;

        width: calc(#{$sidebarWidth} - 16px);
        height: calc(100vh - 48px);
        background-color: gray;
        float: left;
        padding: 8px;

        -webkit-transition: transform 0.2s ease 0s;
        transition: transform 0.2s ease 0s;
    }

    @media (max-width: 480px) {
        .closed {
            transform: translateX(#{-$sidebarWidth});
        }
    }

    .sidebar h3 {
        font-size: 28px;
        padding-top: 16px;
    }

    .select-deck {
        font-size: 24px;
    }

    .category-list-container {
        height: 200px;
        max-height: 380px;
        overflow-y: scroll;
        font-size: 24px;
        background-color: white;
        border-radius: 10px;
    }

    .category-list-container ul {
        padding-left: 8px;
    }

    .category-list-container ul li {
        list-style: none;
    }

    .button {
        width: 100%;
        height: 48px;
        font-size: 24px;
        border-radius: 10px;
        margin-top: 32px;
    }

    .preferences-container {
        font-size: 24px;
        padding-top: 32px;
    }

</style>