package com.klavs.bindle.uix.viewmodel.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.sealedclasses.CommunityRoles
import com.klavs.bindle.data.entity.Event
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityEventListViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val authRepo: AuthRepository,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _unlinkEventResource = MutableStateFlow<Resource<String>>(Resource.Idle())
    val unlinkEventResource = _unlinkEventResource.asStateFlow()

    private val _pastEvents = MutableStateFlow<Resource<List<Event>>>(Resource.Idle())
    val pastEvents = _pastEvents.asStateFlow()

    private var listenCurrentUserJob: Job? = null
    private var listenMyRolePriorityJob: Job? = null

    var lastPastEvent: DocumentSnapshot? = null



fun getPastEvents(communityId: String, pageSize: Int) {
    _pastEvents.value = Resource.Loading()
    viewModelScope.launch(Dispatchers.Main) {
        val linkedEventsRef =
            db.collection("events").whereArrayContains("linkedCommunities", communityId)
                .whereLessThan("date", Timestamp.now())
                .orderBy("date", Query.Direction.DESCENDING)
        val pagedQuerySnapshot = firestoreRepo.getDocumentsWithPaging(
            query = linkedEventsRef,
            pageSize = pageSize.toLong(),
            lastDocument = lastPastEvent
        )
        when (pagedQuerySnapshot) {
            is Resource.Success -> {
                lastPastEvent = if (pagedQuerySnapshot.data!!.size() < pageSize) {
                    null
                } else {
                    pagedQuerySnapshot.data.lastOrNull()
                }
                _pastEvents.value =
                    Resource.Success(data = pagedQuerySnapshot.data.mapNotNull { docSnapshot ->
                        docSnapshot.toObject(Event::class.java).copy(id = docSnapshot.id)
                    }
                    )
            }
            else -> _pastEvents.value = Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
        }
    }

}

    fun unlinkEvent(eventId: String, communityId: String) {
        _unlinkEventResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val eventRef = db.collection("events").document(eventId)
            val state = firestoreRepo.updateField(
                documentRef = eventRef,
                fieldName = "linkedCommunities",
                data = FieldValue.arrayRemove(communityId)
            )
            if (state is Resource.Success){
                _unlinkEventResource.value = Resource.Success(data = eventId)
            }else{
                _unlinkEventResource.value = Resource.Error(messageResource = R.string.something_went_wrong_try_again_later)
            }
        }
    }
}