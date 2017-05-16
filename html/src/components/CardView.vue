<template>
    <div class="card-view">
        <!-- Counter -->
        <div v-if="cards" class="card-counter">Card {{ cardIndex + 1}} of {{ cards.length }}</div>

        <!-- Card container -->
        <div class="card-container">

            <!-- div which contains the card html -->
            <div class="card" v-html="card[side] + currentStyle"></div>
        </div>
    </div>
</template>
<script>
    export default {
        props: ['card', 'cardIndex', 'cards', 'side'],

        computed: {
            /**
             * Returns the style of the current card. The css is wrapped by the start and end of the style tag
             * @returns {string} Css string of current card, null if no card is displayed
             */
            currentStyle () {
                return '<style>' + this.card.style.css + '</style>';
            }
        }
    }
</script>
<style scoped>
    .card-view {
        position: relative;
        flex-grow: 1;
        margin-top: 25px;
        margin-bottom: 8px;
    }

    .card-container {
        position: absolute;
        height: 100%;
        max-height: 100%;
        width: 100%;
        background-color: white;
        margin-top: 25px;
        overflow-y: scroll;
    }

    .card {
        width: calc(100% - 16px);
        font-size: 0.8em !important; /* Override font-size in the card style*/
        flex-grow: 1;
        padding: 8px;
    }

    .card-counter {
        background-color: black;
        color: white;
        font-size: 12pt;
        position: absolute;
        width: initial;
        right: 0;
        padding: 4px;
    }
</style>