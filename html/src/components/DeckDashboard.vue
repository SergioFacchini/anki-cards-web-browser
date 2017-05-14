<template>
    <div class="deck-dashboard">

        <!-- Card container -->
        <div class="card-container">
            <!-- If there is a current card -->
            <div v-if="currentCard" ref="cardContainer">
                <!-- div which contains the card html -->
                <div class="card-html-container card" v-html="currentCard[side] + currentStyle"></div>

                <!-- Counter of card in the bottom right corner -->
                <div v-if="session" class="card-counter">{{ cardIndex }} / {{ cards.length }}</div>
            </div>

            <!-- No current card -->
            <div v-else class="no-deck">
                <div class="card-html-container">
                    <p>Seleziona un mazzo e premi Studia!</p>
                </div>
            </div>
        </div>

        <!-- Row with controls -->
        <div class="control-row">
            <button class="previous" @click="previous" :disabled="!canGoToPrevious">&lt;</button>
            <button class="center-control" v-if="side == 'front'" @click="showAnswer" :disabled="!isCardDisplayed">
                mostra
            </button>
            <button class="center-control" v-if="side == 'rear'" @click="next" :disabled="!canGoToNext">prossima
            </button>
            <button class="next" @click="next" :disabled="!canGoToNext">&gt;</button>
        </div>
    </div>
</template>
<script>
    import EventBus from '../EventBus';
    import Utils from '../Utils';

    export default {
        props: ['cardTypes'],

        data () {
            return {
                cards: null,
                currentCard: null,

                session: false,

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
                return this.cards != null && this.cardIndex < this.cards.length;
            },

            /**
             * Checks if there is a current card
             * @returns {boolean} True if there is a current card
             */
            isCardDisplayed () {
                return this.currentCard != null;
            },

            /**
             * Returns the style of the current card. The css is wrapped by the start and end of the style tag
             * @returns {string} Css string of current card, null if no card is displayed
             */
            currentStyle () {
                const cardCss = this.cardTypes[this.currentCard.typeId].css;
                return '<style>' + cardCss + '</style>';
            }
        },
        methods: {
            /**
             * Method to initialize the component to start a new session
             * @param info Info of the current session
             */
            startSession (info) {
                console.log('[DeckDashboard] Start new session', info);

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
            }
        }
    }
</script>
<style>

    .deck-dashboard {
        width: calc(100% - 32px);
        padding: 16px;
        height: calc(100vh - 48px);
        background-color: slategray;
    }

    .card-container {
        position: relative;
        width: 100%;
        height: 70vh;
        background-color: white;
        border-radius: 25px;
    }

    .card {
        border-radius: 25px;
        overflow-y: scroll;
    }

    .card img {
        max-width: 100%;
    }

    .card-counter {
        position: absolute;
        bottom: 16px;
        right: 16px;
        font-size: 32px;
        color: lightgray;
    }

    .control-row {
        padding-top: 16px;
        display: flex;
        justify-content: space-around;
    }

    .control-row button {
        text-transform: uppercase;
        display: inline-block;
        color: white;
        background-color: black;
        font-size: 32px;
        border-radius: 10px;
        flex-grow: 1;
    }

    .control-row button:disabled {
        background-color: lightgray;
    }

    .card-html-container {
        padding: 32px;
        font-size: 32px;
    }
</style>