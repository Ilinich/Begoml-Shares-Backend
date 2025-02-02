package com.begoml.shares.core

import com.begoml.shares.core.AppQuartzSchedulerImpl.Companion.SHARES_NEWS_UPDATE_SERVICE
import com.begoml.shares.core.AppQuartzSchedulerImpl.Companion.SHARES_PRICE_UPDATE_SERVICE
import com.begoml.shares.presentation.services.SharesNewsUpdateService
import com.begoml.shares.presentation.services.SharesPriceUpdateService
import org.quartz.JobBuilder
import org.quartz.JobExecutionContext
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.quartz.Job

interface AppQuartzScheduler {
    fun start()
}

class AppQuartzSchedulerImpl(
    private val sharesPriceService: SharesPriceUpdateService,
    private val sharesNewsService: SharesNewsUpdateService,
) : AppQuartzScheduler {

    companion object {
        const val SHARES_PRICE_UPDATE_SERVICE = "sharesPriceUpdateService"
        const val SHARES_PRICE_UPDATE_JOB = "sharesPriceUpdateJob"
        const val SHARES_PRICE_UPDATE_GROUP = "group1"
        const val SHARES_PRICE_UPDATE_TRIGGER = "sharesPriceUpdateTrigger"

        const val SHARES_NEWS_UPDATE_SERVICE = "sharesNewsUpdateService"
        const val SHARES_NEWS_UPDATE_JOB = "sharesNewsUpdateJob"
        const val SHARES_NEWS_UPDATE_GROUP = "group2"
        const val SHARES_NEWS_UPDATE_TRIGGER = "sharesNewsUpdateTrigger"
    }

    override fun start() {
        val priceUpdateJob = JobBuilder.newJob(SharesPriceUpdateJob::class.java)
            .withIdentity(SHARES_PRICE_UPDATE_JOB, SHARES_PRICE_UPDATE_GROUP)
            .build()

        val priceUpdateTrigger = TriggerBuilder.newTrigger()
            .withIdentity(SHARES_PRICE_UPDATE_TRIGGER, SHARES_PRICE_UPDATE_GROUP)
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInMinutes(2)
                    .repeatForever()
            )
            .build()

        val newsUpdateJob = JobBuilder.newJob(NewsUpdateJob::class.java)
            .withIdentity(SHARES_NEWS_UPDATE_JOB, SHARES_NEWS_UPDATE_GROUP)
            .build()

        val newsUpdateTrigger = TriggerBuilder.newTrigger()
            .withIdentity(SHARES_NEWS_UPDATE_TRIGGER, SHARES_NEWS_UPDATE_GROUP)
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInHours(12)
                    .repeatForever()
            )
            .build()

        val scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.context[SHARES_PRICE_UPDATE_SERVICE] = sharesPriceService
        scheduler.context[SHARES_NEWS_UPDATE_SERVICE] = sharesNewsService

        scheduler.start()
        scheduler.scheduleJob(priceUpdateJob, priceUpdateTrigger)
        scheduler.scheduleJob(newsUpdateJob, newsUpdateTrigger)
    }
}

class SharesPriceUpdateJob : Job {
    override fun execute(context: JobExecutionContext) {
        val stockPriceUpdater = context.scheduler.context[SHARES_PRICE_UPDATE_SERVICE] as SharesPriceUpdateService
        stockPriceUpdater.update()
    }
}

class NewsUpdateJob : Job {
    override fun execute(context: JobExecutionContext) {
        val newsUpdater = context.scheduler.context[SHARES_NEWS_UPDATE_SERVICE] as SharesNewsUpdateService
        newsUpdater.update()
    }
}