<template>
    <div class="card-view">
        <!-- Counter -->
        <div v-if="cards" class="card-counter">Card {{ cardIndex }} of {{ cards.length }}</div>

        <!-- Card container -->
        <div class="card-container">

            <!-- div which contains the card html -->
            <div class="card-html-container card" v-html="card[side] + currentStyle"></div>
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
                console.log(this.card);
                return '<style>' + this.card.style.css + '</style>';
            }
        }
    }
</script>
<style scoped>
    .card-view {
        position: relative;
        flex-grow: 1;
        margin-top: 24px;
        overflow-y: scroll;
    }

    .card-container {
        position: relative;
        width: 100%;
        background-color: white;
    }

    .card {
        width: calc(100% - 64px);
        overflow-y: scroll;
    }

    .card img {
        max-width: 100%;
    }

    .card-html-container {
        padding: 32px;
        font-size: 32px;
    }

    .card-counter {
        background-color: black;
        color: white;
        font-size: 12pt;
        position: absolute;
        width: initial;
        top: -25px;
        right: 0;
        padding: 4px;
    }
</style>