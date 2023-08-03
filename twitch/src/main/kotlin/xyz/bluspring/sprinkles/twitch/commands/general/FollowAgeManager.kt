package xyz.bluspring.sprinkles.twitch.commands.general

import com.mojang.brigadier.context.CommandContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import xyz.bluspring.sprinkles.platform.twitch.TwitchApi
import xyz.bluspring.sprinkles.twitch.auth.TwitchUserAuth
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import java.net.URI
import kotlin.time.Duration.Companion.minutes

object FollowAgeManager {
    private val cooldowns = mutableMapOf<String, Long>()
    private val followCache = mutableMapOf<String, String>()

    fun sendFollowAge(context: CommandContext<TwitchUser>, username: String): Int {
        val followTime = if (
            !followCache.contains(username) &&
            (
                    !cooldowns.contains(context.source.login)
                            || (System.currentTimeMillis() - cooldowns[context.source.login]!! >= 5.minutes.inWholeMilliseconds)
                    )
        ) {
            val ids = TwitchApi.getUserIds(listOf(username, context.source.sourceChannel))

            val resp =
                TwitchUserAuth.get(URI.create("https://api.twitch.tv/helix/channels/followers?user_id=${ids[username]}&broadcaster_id=${ids[context.source.sourceChannel]}"))

            if (resp == null) {
                context.source.send("Failed to run command!")
                return 0
            }

            val dataList = resp.getAsJsonArray("data")

            if (dataList.isEmpty) {
                context.source.send("You are not following ${context.source.sourceChannel}!")
                return 0
            }

            val data = dataList[0]!!.asJsonObject

            data.get("followed_at").asString
        } else if (followCache.contains(username)) {
            followCache[username]!!
        } else {
            val total = cooldowns[context.source.login]!! + 5.minutes.inWholeMilliseconds

            val duration = Clock.System.now().periodUntil(Instant.fromEpochMilliseconds(total), TimeZone.currentSystemDefault())

            context.source.send("You are currently in cooldown! Please wait another ${duration.minutes} minutes and ${duration.seconds} seconds.")
            return 0
        }

        followCache[username] = followTime
        cooldowns[context.source.login] = System.currentTimeMillis()

        // I would like to reflect on the fact that Kotlin's developers, JetBrains,
        // are fucking legends.

        // I have been stuck on trying to figure out periods for a whole hour.
        // Java's provided Duration and Period classes are so wildly inaccurate they honestly amaze me.
        // And the fact that there are THREE DateTime classes, and FIFTEEN DateTimeFormatters that all do
        // DIFFERENT FUCKING THINGS, I could not for the life of me get a simple fucking period calculator.
        // Every single DateTimeFormatter just didn't support one thing or another.

        // And then after a long time of digging, I finally found the kotlinx.datetime classes.
        // And guess fucking what, what you see right now is ALL I NEEDED to get it working.
        // And it is significantly more accurate than whatever the fuck Java was doing.

        // I hate Java.

        val instant = Instant.parse(followTime)
        val dateTimePeriod = instant.periodUntil(Clock.System.now(), TimeZone.currentSystemDefault())

        val years = dateTimePeriod.years
        val months = dateTimePeriod.months
        val days = dateTimePeriod.days
        val hours = dateTimePeriod.hours
        val minutes = dateTimePeriod.minutes
        val seconds = dateTimePeriod.seconds

        var formatting = ""

        if (years > 0)
            formatting += String.format("%d years ", years)

        if (months > 0)
            formatting += String.format("%d months ", months)

        if (days > 0)
            formatting += String.format("%d days ", days)

        if (hours > 0)
            formatting += String.format("%d hours ", hours)

        if (years == 0 && months == 0 && days == 0) {
            if (minutes > 0)
                formatting += String.format("%d mins ", minutes)

            if (seconds > 0)
                formatting += String.format("%d secs ", seconds)
        }

        if (formatting == "")
            formatting += "congratulations. you have found an easter egg. how have you managed this."

        context.source.send(
            "$username has been following ${context.source.sourceChannel} for $formatting"
        )

        return 1
    }
}