export default {

    /**
     * Shuffles the array
     * @param array Array to shuffle
     */
    shuffleArray (array) {
        var i = array.length, tmp, randomIndex;

        while (0 !== i) {

            // Select a random element
            randomIndex = Math.floor(Math.random() * i);
            i -= 1;

            // Swap the object at the random index with the object at the i
            tmp = array[i];
            array[i] = array[randomIndex];
            array[randomIndex] = tmp;
        }
    },

    /**
     * Check if a card is present in a array of decks given its id
     * @param decks Array of decks to which search in
     * @param id Id of the wanted card
     * @returns {*} The card object or null if not present
     */
    getCardInDecksById (decks, id) {
        // Create an array with all the cards
        const cards = decks.map(deck => {
            if (deck.hasCategories) {
                return deck.categories
                    .map(category => category.cards)
                    .reduce((a, b) => a.concat(b))
            } else {
                return deck.cards;
            }
        }).reduce((a, b) => a.concat(b));

        for (let i in cards) {
            let card = cards[i];
            if (card.id == id) {
                return card;
            }
        }


        return null;
    }
}