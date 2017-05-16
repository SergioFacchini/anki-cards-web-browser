<template>
    <div class="sidebar" ref="sidebar">

        <!-- Container of the session choices -->
        <div class="session-setup" v-if="decks != null">

            <!-- Select deck -->
            <h3>Select a deck to study</h3>
            <div class="select-deck-container">
                <div>
                    <select class="select-deck" v-model="selectedDeck" :disabled="session">
                        <option v-for="deck in decks" :value="deck">{{ deck.name }}</option>
                    </select>
                </div>
            </div>

            <!-- Category box -->
            <div v-if="selectedDeck.hasCategories">
                <h3>Select categories</h3>
                <div class="category-list-container">
                    <ul class="category-list">
                        <li v-for="category in selectedDeck.categories">
                            <label><input type="checkbox" v-model="selectedCategories" :disabled="session"
                                          :value="category.categoryName">{{ category.categoryName || "(no category)" }}</label>
                        </li>
                    </ul>
                </div>

                <div class="category-list-buttons">
                    <button @click="invertCategories" :disabled="session">Invert</button>
                    <button v-if="!allCategoriesSelected" @click="selectAllCategories" :disabled="session">Select all
                    </button>
                    <button v-if="allCategoriesSelected" @click="deselectAllCategories" :disabled="session">Deselect all
                    </button>
                </div>
            </div>

            <!-- Random toggle -->
            <div class="preferences-container">
                <label><input type="checkbox" v-model="randomizeCards" :disabled="session">Shuffle cards</label>
            </div>

            <!-- Start to study button -->
            <div v-if="!session">
                <button class="full-width" :disabled="!canStart" @click="start">Start studying</button>
            </div>
            <div class="session-info" v-else>
                <button class="full-width" @click="stopSession">Stop</button>
            </div>
        </div>

        <!-- Show loading message -->
        <div class="loading-box" v-else>
            <p>Loading</p>
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
            EventBus.$on('closeSidebar', () => this.closeSidebar());

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

            allCategoriesSelected () {
                return this.selectedDeck.categories.filter(category => {
                        return this.selectedCategories.indexOf(category.categoryName) < 0;
                    }).length == 0;
            }
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
            },

            invertCategories () {
                this.selectedDeck.categories.forEach(category => {
                    const index = this.selectedCategories.indexOf(category.categoryName);

                    if (index < 0) {
                        this.selectedCategories.push(category.categoryName);
                    } else {
                        this.selectedCategories.splice(index, 1);
                    }
                });
            },

            selectAllCategories () {
                this.selectedDeck.categories.forEach(category => {
                    if (this.selectedCategories.indexOf(category.categoryName) < 0) {
                        this.selectedCategories.push(category.categoryName);
                    }
                });
            },

            deselectAllCategories () {
                this.selectedCategories.splice(0, this.selectedCategories.length);
            }
        }
    }
</script>
<style lang="scss" type="text/scss" scoped>

    @import "../style-settings.scss";

    .sidebar {
        position: absolute;
        z-index: 1;

        width: calc(#{$sidebarWidth} - 32px);
        height: calc(100vh - 78px - 16px);
        background-color: #404040;
        float: left;
        padding: 8px 16px;

        overflow-y: scroll;

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
        text-align: center;
        color: white;
    }

    .select-deck {
        font-size: 24px;
        width: calc(#{$sidebarWidth} - 32px);
        max-width: calc(#{$sidebarWidth} - 32px);
        border-radius: 0;
        padding-left: 8px;
        -webkit-appearance: none;
        background-color: white;
    }

    .category-list-container {
        height: 200px;
        max-height: 380px;
        overflow-y: scroll;
        font-size: 18px;
        background-color: white;
        color: white;
        border: solid 1px slategray;
    }

    .category-list {
        padding-left: 0 !important;
        margin: 0;
    }

    .category-list li {
        height: 24px;
    }

    .category-list li label input {
        margin-right: 8px;
        position: relative;
    }

    .category-list li:nth-child(even) {
        background-color: #282828;
    }

    .category-list li:nth-child(odd) {
        background-color: #303030;
    }

    .category-list-container ul {
        padding-left: 8px;
    }

    .category-list-container ul li {
        list-style: none;
    }

    .full-width {
        width: 100%;
    }

    .category-list-buttons {
        display: flex;
        justify-content: space-between;
    }

    .preferences-container {
        font-size: 24px;
        padding-top: 32px;
        color: white;
    }

</style>