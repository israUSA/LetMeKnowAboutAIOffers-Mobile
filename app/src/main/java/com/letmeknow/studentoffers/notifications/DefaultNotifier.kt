package com.letmeknow.studentoffers.notifications

import android.content.Context
import com.letmeknow.studentoffers.domain.model.Promo

class DefaultNotifier(private val context: Context) : Notifier {

    private val presenter: PromoNotificationPresenter by lazy {
        PromoNotificationPresenter(context)
    }

    override suspend fun notifyNewPromos(promos: List<Promo>) {
        if (promos.isEmpty()) return
        presenter.showNewPromos(promos)
    }
}
