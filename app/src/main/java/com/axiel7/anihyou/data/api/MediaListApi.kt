package com.axiel7.anihyou.data.api

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.axiel7.anihyou.DeleteMediaListMutation
import com.axiel7.anihyou.MediaListCustomListsQuery
import com.axiel7.anihyou.MediaListIdsQuery
import com.axiel7.anihyou.MediaListWidgetQuery
import com.axiel7.anihyou.UpdateEntryMutation
import com.axiel7.anihyou.UpdateEntryProgressMutation
import com.axiel7.anihyou.UserListCollectionQuery
import com.axiel7.anihyou.fragment.BasicMediaListEntry
import com.axiel7.anihyou.fragment.BasicMediaListEntryImpl
import com.axiel7.anihyou.type.FuzzyDateInput
import com.axiel7.anihyou.type.MediaListSort
import com.axiel7.anihyou.type.MediaListStatus
import com.axiel7.anihyou.type.MediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaListApi @Inject constructor(
    private val client: ApolloClient
) {
    fun mediaListCollection(
        userId: Int,
        mediaType: MediaType,
        sort: List<MediaListSort>,
        fetchFromNetwork: Boolean,
        chunk: Int,
        perChunk: Int
    ) = client
        .query(
            UserListCollectionQuery(
                userId = Optional.present(userId),
                type = Optional.present(mediaType),
                sort = Optional.present(sort),
                chunk = Optional.present(chunk),
                perChunk = Optional.present(perChunk)
            )
        )
        .fetchPolicy(if (fetchFromNetwork) FetchPolicy.NetworkFirst else FetchPolicy.CacheFirst)

    fun mediaListWidget(
        userId: Int,
        mediaType: MediaType,
        page: Int,
        perPage: Int
    ) = client
        .query(
            MediaListWidgetQuery(
                userId = Optional.present(userId),
                type = Optional.present(mediaType),
                page = Optional.present(page),
                perPage = Optional.present(perPage)
            )
        )

    suspend fun updateMediaListCache(data: BasicMediaListEntry) {
        client.apolloStore
            .writeFragment(
                fragment = BasicMediaListEntryImpl(),
                cacheKey = CacheKey("${data.id} ${data.mediaId}"),
                fragmentData = data,
            )
    }

    fun updateEntryProgressMutation(
        entryId: Int,
        progress: Int
    ) = client
        .mutation(
            UpdateEntryProgressMutation(
                saveMediaListEntryId = Optional.present(entryId),
                progress = Optional.present(progress)
            )
        )

    fun updateEntryMutation(
        mediaId: Int,
        status: MediaListStatus?,
        score: Double?,
        advancedScores: List<Double>?,
        progress: Int?,
        progressVolumes: Int?,
        startedAt: FuzzyDateInput?,
        completedAt: FuzzyDateInput?,
        repeat: Int?,
        private: Boolean?,
        hiddenFromStatusLists: Boolean?,
        notes: String?,
        customLists: List<String?>?,
    ) = client
        .mutation(
            UpdateEntryMutation(
                mediaId = Optional.present(mediaId),
                status = Optional.presentIfNotNull(status),
                score = Optional.presentIfNotNull(score),
                advancedScores = Optional.presentIfNotNull(advancedScores),
                progress = Optional.presentIfNotNull(progress),
                progressVolumes = Optional.presentIfNotNull(progressVolumes),
                startedAt = Optional.presentIfNotNull(startedAt),
                completedAt = Optional.presentIfNotNull(completedAt),
                repeat = Optional.presentIfNotNull(repeat),
                private = Optional.presentIfNotNull(private),
                hiddenFromStatusLists = Optional.presentIfNotNull(hiddenFromStatusLists),
                notes = Optional.presentIfNotNull(notes),
                customLists = Optional.presentIfNotNull(customLists),
            )
        )

    fun deleteMediaListMutation(id: Int) = client
        .mutation(
            DeleteMediaListMutation(
                mediaListEntryId = Optional.present(id)
            )
        )

    fun mediaListCustomLists(
        id: Int,
        userId: Int
    ) = client
        .query(
            MediaListCustomListsQuery(
                id = Optional.present(id),
                userId = Optional.present(userId)
            )
        )

    fun mediaListIds(
        userId: Int,
        type: MediaType,
        status: MediaListStatus?,
        chunk: Int,
        perChunk: Int
    ) = client
        .query(
            MediaListIdsQuery(
                type = Optional.present(type),
                userId = Optional.present(userId),
                status = Optional.presentIfNotNull(status),
                chunk = Optional.present(chunk),
                perChunk = Optional.present(perChunk)
            )
        )
}