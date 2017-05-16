<template>
    <div class="deck-dashboard">
        <div v-if="currentCard" class="card-and-controls">
            <!-- Over the card -->
            <div class="over-card">
                <h2>{{ currentCard.deck.name }}</h2>
                <p v-if="session && currentCard.deck.hasCategories">({{ selectedCategoriesCount }} of {{
                    currentCard.deck.categories.length }}
                    categories selected)</p>
            </div>

            <!-- Card -->
            <card-view :card="currentCard" :side="side" :card-index="cardIndex" :cards="cards"></card-view>

            <!-- Row with controls -->
            <div class="control-row">
                <!-- Previous -->
                <button class="previous" @click="previous" :disabled="!canGoToPrevious">&lt;</button>

                <!-- Start center -->
                <!-- Show answer -->
                <button class="center-control" v-if="side == 'front'" @click="showAnswer" :disabled="!isCardDisplayed">
                    Show
                </button>
                <!-- Next on center -->
                <button class="center-control" v-if="side == 'rear' && !last" @click="next" :disabled="!canGoToNext">Next
                </button>

                <!-- End -->
                <button class="center-control" v-if="side == 'rear' && last" @click="end">End
                </button>
                <!-- End center -->

                <!-- Next -->
                <button class="next" @click="next" :disabled="!canGoToNext">&gt;</button>
            </div>
        </div>
        <div v-else class="no-session">
            <i class="material-icons" @click="openSidebar">keyboard_arrow_left</i>
            <p>Pick a deck from the sidebar</p>
        </div>
    </div>
</template>
<script>
    import CardView from './CardView.vue';
    import EventBus from '../EventBus';
    import Utils from '../Utils';

    export default {
        props: ['cardTypes'],

        components: {
            CardView
        },

        data () {
            return {
                cards: null,
                currentCard: null,

                session: false,
                selectedCategoriesCount: -1,

                // Index of the current card in the cards array
                cardIndex: -1,

                // Side of the current card
                side: 'front'
            }
        },

        mounted () {
            // Listen to the start event
            EventBus.$on('start', (cards) => this.startSession(cards));

            EventBus.$on('stop', () => {
                this.session = false;
                this.showCard(null);
                this.cards = null;
                this.cardIndex = -1;
            });

            // Listen to the download completed event
            EventBus.$on('downloadCompleted', (data) => {

                // Check if in the url there is a card id
                const path = location.pathname;
                if (path.length > 1) {
                    // Remove slash
                    const id = path.replace('/', '');

                    // Check if we have the card with this id
                    const card = Utils.getCardInDecksById(data.decks, id);

                    if (card != null) {
                        this.showCard(card);
                        EventBus.$emit('closeSidebar');
                    }
                }
            });
        },
        computed: {

            /**
             * Return true if the user can go to the previous card
             * @returns {boolean} True if the user can go to previous
             */
            canGoToPrevious () {
                return this.cards != null && this.cardIndex > 0;
            },
            canGoToNext () {
                return this.cards != null && (this.cardIndex + 1) < this.cards.length;
            },

            /**
             * Checks if there is a current card
             * @returns {boolean} True if there is a current card
             */
            isCardDisplayed () {
                return this.currentCard != null;
            },

            /**
             * This method checks if the current card is the last of a session. If only the card
             * is shown and we are not in a session, the card is not the last.
             */
            last () {
                return this.session && (this.cardIndex + 1) >= this.cards.length;
            }
        },
        methods: {
            /**
             * Method to initialize the component to start a new session
             * @param info Info of the current session
             */
            startSession (info) {
                console.log('[DeckDashboard] Start new session', info);

                this.selectedCategoriesCount = info.selectedCategories.length;

                // Create an array of current cards
                if (info.deck.hasCategories) {
                    this.cards = info.selectedCategories
                        .map(category => category.cards)
                        .reduce((a, b) => a.concat(b));
                } else {
                    this.cards = info.deck.cards;
                }

                // Randomize if necessary
                if (info.randomize) {
                    Utils.shuffleArray(this.cards);
                }

                // Update the index
                this.showCardAt(0);
                this.session = true;
            },

            /**
             * Update the data object to show the card at the specified index
             * @param index Index of the card to show
             */
            showCardAt(index) {
                this.cardIndex = index;
                this.showCard(this.cards[this.cardIndex]);
            },

            showCard (card) {
                this.currentCard = card;
                this.side = 'front';

                // Update the url
                window.history.replaceState(null, null, card != null ? card.id : '');
            },

            previous () {
                this.showCardAt(this.cardIndex - 1);
            },

            showAnswer () {
                this.side = 'rear';
            },

            next () {
                this.showCardAt(this.cardIndex + 1);
            },

            openSidebar (evt) {
                evt.stopPropagation();
                EventBus.$emit('openSidebar');
            },

            end () {
                EventBus.$emit('stop');
            }
        }
    }
</script>
<style>

    .deck-dashboard {
        width: calc(100% - 32px);
        padding: 16px;
        height: calc(100% - 32px);
        background-color: #bfbfbf;
        text-align: center;
    }

    .deck-dashboard div {
        width: 100%;
        font-size: 24px;
    }

    .no-session i {
        font-size: 256px;
    }

    .over-card h2 {
        margin: 8px;
        font-size: 24px;
    }

    .over-card p {
        margin: 4px;
        font-size: 18px;
    }

    .card-and-controls {
        height: 100%;
        max-height: 100%;
        display: flex;
        flex-direction: column;
    }

    .control-row {
        display: flex;
        justify-content: space-around;
    }

    .control-row button {
        flex-grow: 1;
        margin-bottom: 8px;
    }

    .center-control {
        margin-left: 16px;
        margin-right: 16px;
    }
</style>