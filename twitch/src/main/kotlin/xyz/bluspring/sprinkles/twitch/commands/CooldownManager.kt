package xyz.bluspring.sprinkles.twitch.commands

object CooldownManager {
    // command root -> timestamp
    private val lastGlobalRun: MutableMap<String, Long> = mutableMapOf()
    // login & command root -> timestamp
    private val lastUserRun: MutableMap<Pair<String, String>, Long> = mutableMapOf()

    fun isWithinCooldown(login: String, root: String, globalCooldown: Long, userCooldown: Long): Boolean {
        val pair = Pair(login, root)

        if (!lastGlobalRun.contains(root) && !lastUserRun.contains(pair))
            return true

        val global = lastGlobalRun[root] ?: 0L
        val user = lastUserRun[pair] ?: 0L

        return System.currentTimeMillis() - global >= globalCooldown && System.currentTimeMillis() - user >= userCooldown
    }

    fun isWithinCooldown(login: String, root: String, cooldown: Long): Boolean {
        return isWithinCooldown(login, root, cooldown, cooldown)
    }

    fun triggerCooldown(login: String, root: String) {
        lastUserRun[Pair(login, root)] = System.currentTimeMillis()
        lastGlobalRun[root] = System.currentTimeMillis()
    }
}