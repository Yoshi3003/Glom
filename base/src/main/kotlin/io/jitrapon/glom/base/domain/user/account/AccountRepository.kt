package io.jitrapon.glom.base.domain.user.account

import io.jitrapon.glom.base.repository.Repository
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by Jitrapon
 */
class AccountRepository(private val localDataSource: AccountDataSource,
                        private val remoteDataSource: AccountDataSource)
    : Repository<AccountInfo>(), AccountDataSource {

    override fun initAccount(): Completable {
        return localDataSource.initAccount()
    }

    override fun getAccount(): AccountInfo? = localDataSource.getAccount()

    override fun refreshToken(refreshToken: String?): Flowable<AccountInfo> {
        return load(true,
                localDataSource.refreshToken(),
                remoteDataSource.refreshToken(localDataSource.getAccount()?.refreshToken),
                localDataSource::saveAccount)
    }

    override fun saveAccount(account: AccountInfo): Flowable<AccountInfo> {
        return localDataSource.saveAccount(account)
    }
}