<template>
    <div class="topbar">
        <!-- Hamburger -->
        <div class="toggle-sidebar-icon" @click="toggleSidebar">
            <span></span>
            <span></span>
            <span></span>
        </div>

        <!-- App title -->
        <h1 class="app-title">Anki Cards Web Browser</h1>
    </div>
</template>
<script>
    import EventBus from '../EventBus';

    export default {
        methods: {
            /**
             * Method called from the UI
             */
            toggleSidebar () {
                // Emit the event 'toggleSidebar' on the global bus
                EventBus.$emit('toggleSidebar');
            }
        }
    }
</script>
<style lang="scss" scoped>

    @import "../style-settings.scss";

    .topbar {
        background-color: black;
        height: $topbarHeight;
        width: calc(100% - 16px);

        padding: 8px;
    }

    .app-title {
        font-weight: 600;
        font-size: 24px;
        color: white;
        margin-top: 0;
        padding-top: 16px;
        text-align: center;
    }

    .toggle-sidebar-icon {
        position: relative;
        height: 32px;
        width: 32px;
        display: inline-block;
        float: left;
        margin-right: 8px;
        margin-top: 12px;
        border-radius: 10px;
    }

    .toggle-sidebar-icon span {
        width: 24px;
        height: 4px;
        background-color: white;
        margin: 5px;
        position: absolute;
    }

    .toggle-sidebar-icon span:nth-child(1) {
        top: 4px;
    }

    .toggle-sidebar-icon span:nth-child(2) {
        top: 10px;
    }

    .toggle-sidebar-icon span:nth-child(3) {
        top: 16px;
    }

    @media (min-width: 480px) {
        .toggle-sidebar-icon {
            display: none;
        }
    }
</style>