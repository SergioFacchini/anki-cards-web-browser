<template>
    <div class="card-view">
        <!-- Counter -->
        <div v-if="card.categoryName" class="etiquette category-name">{{ card.categoryName }}</div>
        <div v-if="cards" class="etiquette card-counter">Card {{ cardIndex + 1}} of {{ cards.length }}</div>

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

    .card img {
        max-width: 100%;
    }

    .etiquette {
        position: absolute;
        background-color: black;
        color: white;
        font-size: 12pt;
        width: initial;
        padding: 4px;
    }

    .card-counter {
        right: 0;
    }

    .category-name {
        left: 0;
    }

</style>