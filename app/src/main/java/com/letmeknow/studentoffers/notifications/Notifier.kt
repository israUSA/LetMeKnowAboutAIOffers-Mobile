package com.letmeknow.studentoffers.notifications

import com.letmeknow.studentoffers.domain.model.Promo

interface Notifier {

    suspend fun notifyNewPromos(promos: List<Promo>)
}
