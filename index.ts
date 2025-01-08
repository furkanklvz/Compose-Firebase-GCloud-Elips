import * as functions from "firebase-functions/v2";
import * as admin from "firebase-admin";
import { Timestamp } from "firebase-admin/firestore";

admin.initializeApp();
const db = admin.firestore();


export const onEventCanceled = functions.firestore
  .onDocumentDeleted("events/{eventId}", async (event) => {
    const eventId = event.params.eventId;
    const eventName = event.data?.get("title") as string;

    const participantsSnapshot = await db
      .collection("events")
      .doc(eventId)
      .collection("participants")
      .get();

    await Promise.all(
      participantsSnapshot.docs.map(async (participantSnapshot) => {
        const userSnapshot = await db
          .collection("users")
          .doc(participantSnapshot.id)
          .get();

        const fcmToken = userSnapshot.get("fcmToken") as string
        const language = userSnapshot.get("language") as string || "en"

        if (fcmToken) {
          let notificationTitle;
          let notificationBody;
          switch (language) {
            case "tr":
              notificationTitle = `${eventName} İptal Edildi ❌`;
              notificationBody = `Görünüşe göre etkinliğiniz organizatör tarafından iptal edildi. Yeni etkinlikler keşfetmek için Bindle'a göz atın.`;
              break;

            default:
              notificationTitle = `${eventName} Cancelled ❌`;
              notificationBody = `It looks like your event has been cancelled by the organiser. Check out Bindle to explore new events.`;
              break;
          }
          const message = {
            token: fcmToken,
            notification: {
              title: notificationTitle,
              body: notificationBody
            },
            data: {
              type: "event_cancelled"
            },
            android: {
              priority: "high" as "high" | "normal" | undefined
            }
          };

          try {
            await admin.messaging().send(message);
          } catch (error) {
            console.error("Bildirim gönderme hatası:", error);
          }
        }
      })
    )
  })

export const checkEvents = functions.scheduler.onSchedule("every 1 minutes", async () => {
  try {
    const now = Timestamp.now()
    const eventsSnapshots = await db
      .collection("events")
      .where("notificationsSent", "==", false)
      .where("date", "<=", now)
      .get();
    if (eventsSnapshots.empty) {
      return;
    }

    await Promise.all(
      eventsSnapshots.docs.map(async (eventSnapshot) => {
        const eventTitle = eventSnapshot.get("title") as string

        const participantsSnapshot = await db
          .collection("events")
          .doc(eventSnapshot.id)
          .collection("participants")
          .get();

        await Promise.all(participantsSnapshot.docs.map(async (participantSnapshot) => {
          const uid = participantSnapshot.id;
          const userSnapshot = await db
            .collection("users")
            .doc(uid)
            .get();

          const fcmToken = userSnapshot.get("fcmToken") as string;
          const language = userSnapshot.get("language") as string || "en";

          if (fcmToken) {
            let notificationTitle;
          let notificationBody;
          switch (language) {
            case "tr":
              notificationTitle = `Etkinlik Zamanı Geldi 🎉`;
              notificationBody = `${eventTitle} başladı! Geç kalmayın, güzel bir gün sizi bekliyor.`
              break;

            default:
              notificationTitle = `Is's Event Time 🎉`;
              notificationBody = `${eventTitle} has started! Don't be late, a wonderful day awaits you.`
              break;
          }
          const message = {
            token: fcmToken,
            notification: {
              title: notificationTitle,
              body: notificationBody
            },
            data: {
              eventId: eventSnapshot.id,
              type: "event_has_started"
            },
            android: {
              notification: {
                clickAction: "OPEN_EVENT"
              },
              priority: "high" as "high" | "normal" | undefined
            }
          };

          try {
            await admin.messaging().send(message);
          } catch (error) {
            console.error("Bildirim gönderme hatası:", error);
          }
          }

          
        }));
        await db
          .collection("events")
          .doc(eventSnapshot.id)
          .update({ notificationsSent: true })
      })
    );

  } catch (error) {
    console.error("checkEvents hatası:", error);
    return;
  }
});

// 'events/{eventId}/chat/{messageId}' koleksiyonuna yeni bir mesaj eklendiğinde tetiklenir
export const onNewChatMessage = functions.firestore
  .onDocumentCreated("events/{eventId}/messages/{messageId}", async (event) => {
    const newMessage = event.data?.data(); // Yeni eklenen mesaj verisini al
    const eventId = event.params.eventId; // Event ID'si
    const senderUid = newMessage?.senderUid || '';
    const message = newMessage?.message || '';
    const senderUsername = newMessage?.senderUsername || '';


    // Katılımcılara bildirim gönderme işlemi
    try {
      const participantsSnapshot = await db
        .collection(`events/${eventId}/participants`)
        .where('uid', '!=', senderUid)
        .get();
      console.log(`bildirim gonderilecek kisi sayisi: ${participantsSnapshot.size}`);

      const eventSnapshot = await db
        .collection(`events`)
        .doc(`${eventId}`)
        .get();

      var eventTitle = eventSnapshot.get("title") as string


      // Katılımcıların fcmToken'larını alıp, bildirim göndermek için Promise.all ile paralel işleme
      const promises = participantsSnapshot.docs.map(async (doc) => {
        const participantUid = doc.id; // Katılımcı UID'si

        // Katılımcının fcmToken'ını almak için 'users' koleksiyonunu sorguluyoruz
        const userDoc = await admin
          .firestore()
          .collection("users")
          .doc(participantUid)
          .get();

        const participantToken = userDoc.get("fcmToken") as string || "";
        const participantLang = userDoc.get("language") as string || 'en';


        let title;
        let body;
        switch (participantLang) {
          case "tr":
            title = `${eventTitle} Etkinliği`
            body = `${senderUsername}: ${message}`
            break;

          default:
            title = `${eventTitle} Event`
            body = `${senderUsername}: ${message}`
            break;
        }

        if (participantToken) {
          // Katılımcıya bildirim gönderme
          return sendChatNotification(
            participantToken,
            title,
            body,
            eventId,
            "chat",
            "OPEN_CHAT",
            "high");
        } else {
          console.log(`FCM token bulunamadi: ${participantUid}`);
          return null;
        }
      });

      // Tüm bildirim gönderme işlemlerini paralel olarak yap
      await Promise.all(promises);
      console.log("Tüm katilimcilara bildirim gönderildi");

    } catch (error) {
      console.error("Katilimcilara bildirim gönderirken hata oluştu:", error);
    }
  });

export const onNewEventInCommunity = functions.firestore
  .onDocumentCreated("events/{eventId}", async (event) => {
    const newEvent = event.data?.data();
    if (!newEvent) {
      console.error("Yeni etkinlik verisi alınamadı.");
      return;
    }
    const eventOwnerUid = newEvent.ownerUid
    const eventTitle = newEvent.title;
    const linkedCommunities = newEvent.linkedCommunities as Array<string> | undefined;

    if (!linkedCommunities || linkedCommunities.length === 0) {
      console.log("linkedCommunities boş veya tanımsız.");
      return;
    }

    await Promise.all(
      linkedCommunities.map(async (communityId) => {
        const communitySnapshot = await admin
          .firestore()
          .collection("communities")
          .doc(communityId)
          .get();

        const communityName =
          (communitySnapshot.get("name") as string) || "Unnamed Community";

        const membersSnapshot = await admin
          .firestore()
          .collection("communities")
          .doc(communityId)
          .collection("members")
          .get();

        const memberPromises = membersSnapshot.docs.map(async (doc) => {
          const memberUid = doc.id;
          if (memberUid === eventOwnerUid) {
            return;
          }


          const userSnapshot = await admin
            .firestore()
            .collection("users")
            .doc(memberUid)
            .get();

          const fcmToken = userSnapshot.get("fcmToken") as string | undefined;
          const lang = (userSnapshot.get("language") as string) || "en";



          let title;
          let body;
          switch (lang) {
            case "tr":
              title = `Yeni Etkinlik: ${eventTitle}`;
              body = `${communityName} topluluğunda yeni bir etkinlik düzenleniyor.`;
              break;

            default:
              title = `New Event: ${eventTitle}`;
              body = `A new event is being held in the ${communityName} community.`;
              break;
          }

          if (fcmToken) {
            const message = {
              token: fcmToken,
              notification: { title, body },
              data: {
                communityId,
                type: "new_event_in_community",
              },
              android: {
                notification: { clickAction: "OPEN_COMMUNITY" },
                priority: "high" as const,
              },
            };

            try {
              await admin.messaging().send(message);
              console.log("Bildirim gönderildi:", fcmToken);
            } catch (error) {
              console.error("Bildirim gönderme hatası:", error);
            }
          } else {
            console.warn(`Kullanıcı FCM tokeni eksik: ${memberUid}`);
          }
        });

        await Promise.all(memberPromises);
        console.log(`${communityName} için tüm katılımcılara bildirim gönderildi.`);
      })
    );
  });


export const onNewRequestForEvent = functions.firestore
  .onDocumentCreated("events/{eventId}/requests/{requestId}", async (event) => {
    const newRequest = event.data?.data();
    const eventId = event.params.eventId;

    if (!newRequest || !eventId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }

    const senderUid = newRequest.uid || '';
    if (!senderUid) {
      console.log("Gönderen kullanıcı bilgisi bulunamadı");
      return null; // Değer döndürülüyor
    }

    try {
      // Etkinlik bilgilerini al
      const eventSnapshot = await db.collection('events').doc(eventId).get();
      if (!eventSnapshot.exists) {
        console.error("Etkinlik bulunamadı");
        return null; // Değer döndürülüyor
      }
      const eventTitle = eventSnapshot.get("title") as string;
      const eventOwnerUid = eventSnapshot.get("ownerUid") as string;

      // İstek yapan kullanıcı bilgilerini al
      const requestedUserSnapshot = await db.collection('users').doc(senderUid).get();
      const senderUsername = requestedUserSnapshot.get("userName");

      // Etkinlik sahibinin bilgilerini al
      const eventOwnerSnapshot = await db.collection('users').doc(eventOwnerUid).get();
      const eventOwnerToken = eventOwnerSnapshot.get("fcmToken") as string;
      const eventOwnerLanguage = eventOwnerSnapshot.get("language") as string || 'en';

      // Bildirim metni oluştur
      let title;
      let body;
      switch (eventOwnerLanguage) {
        case 'tr':
          title = `Yeni Katılım Talebi`;
          body = `${senderUsername}, ${eventTitle} etkinliğine katılmak istiyor.`;
          break;
        default:
          title = `New Participation Request`;
          body = `${senderUsername} sent a request to participate in the ${eventTitle} event.`;
          break;
      }

      // Bildirim gönder
      if (eventOwnerToken) {
        await sendEventNotification(
          eventOwnerToken,
          title,
          body,
          eventId,
          "event_request",
          "OPEN_EVENT",
          "normal");
        return null; // Başarılı bir şekilde tamamlandığında da null döndürülüyor
      } else {
        console.log(`FCM token bulunamadı: ${eventOwnerUid}`);
        return null; // Değer döndürülüyor
      }
    } catch (error) {
      console.error("Bildirim gönderirken hata oluştu:", error);
      return null; // Hata durumunda da değer döndürülüyor
    }
  });

export const onNewParticipantForEvent = functions.firestore
  .onDocumentCreated("events/{eventId}/participants/{participantId}", async (event) => {
    const newParticipant = event.data?.data();
    const eventId = event.params.eventId;

    if (!newParticipant || !eventId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }

    const participantUid = newParticipant.uid || '';
    if (!participantUid) {
      console.log("Gönderen kullanıcı bilgisi bulunamadı");
      return null; // Değer döndürülüyor
    }

    try {
      // Etkinlik bilgilerini al
      const eventSnapshot = await db.collection('events').doc(eventId).get();
      if (!eventSnapshot.exists) {
        console.error("Etkinlik bulunamadı");
        return null; // Değer döndürülüyor
      }

      const eventOwnerUid = eventSnapshot.get("ownerUid") as string;
      if (eventOwnerUid == participantUid) {
        console.error("Etkinlik oluşturuldu");
        return null;
      }

      const eventTitle = eventSnapshot.get("title") as string;
      const onlyByRequest = eventSnapshot.get("onlyByRequest") as boolean;


      // İstek yapan kullanıcı bilgilerini al
      const participatedUserSnapshot = await db.collection('users').doc(participantUid).get();
      const participantUsername = participatedUserSnapshot.get("userName");
      const participantToken = participatedUserSnapshot.get("fcmToken") as string | undefined;
      const participantLang = participatedUserSnapshot.get("language") as string || 'en';

      // Etkinlik sahibinin bilgilerini al
      const eventOwnerSnapshot = await db.collection('users').doc(eventOwnerUid).get();
      const eventOwnerToken = eventOwnerSnapshot.get("fcmToken") as string | undefined;
      const eventOwnerLanguage = eventOwnerSnapshot.get("language") as string || 'en';

      // Bildirim metni oluştur
      let titleForEventOwner;
      let bodyForEventOwner;
      switch (eventOwnerLanguage) {
        case 'tr':
          titleForEventOwner = `Yeni Katılımcı`;
          bodyForEventOwner = `${participantUsername}, ${eventTitle} etkinliğine katıldı.`;
          break;
        default:
          titleForEventOwner = `New Participant`;
          bodyForEventOwner = `${participantUsername} participated in the ${eventTitle} event.`;
          break;
      }

      // Bildirim gönder
      if (eventOwnerToken && !onlyByRequest) {
        await sendEventNotification(
          eventOwnerToken,
          titleForEventOwner,
          bodyForEventOwner,
          eventId,
          "event_participation",
          "OPEN_EVENT",
          "normal");
      } else {
        console.log(`FCM token bulunamadı: ${eventOwnerUid}`);
      }

      let titleForParticipant;
      let bodyForParticipant;
      switch (participantLang) {
        case 'tr':
          titleForParticipant = `${eventTitle}`;
          bodyForParticipant = `${eventTitle} etkinliğine katılım isteğin kabul edildi.`;
          break;
        default:
          titleForParticipant = `${eventTitle}`;
          bodyForParticipant = `Your participation request for ${eventTitle} event has accepted.`;
          break;
      }
      // Bildirim gönder
      if (participantToken && onlyByRequest) {
        await sendEventNotification(
          participantToken,
          titleForParticipant,
          bodyForParticipant,
          eventId,
          "event_participation",
          "OPEN_EVENT",
          "normal");
      } else {
        console.log(`FCM token bulunamadı: ${eventOwnerUid}`);
      }
      return null;
    } catch (error) {
      console.error("Bildirim gönderirken hata oluştu:", error);
      return null; // Hata durumunda da değer döndürülüyor
    }
  });

export const onNewMemberForCommunity = functions.firestore
  .onDocumentCreated("communities/{communityId}/members/{memberId}", async (event) => {
    const newMember = event.data?.data();
    const communityId = event.params.communityId;

    if (!newMember || !communityId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }

    const memberUid = newMember.uid || '';
    if (!memberUid) {
      console.log("Gönderen kullanıcı bilgisi bulunamadı");
      return null; // Değer döndürülüyor
    }

    // Etkinlik bilgilerini al
    const communitySnapshot = await db.collection('communities').doc(communityId).get();
    if (!communitySnapshot.exists) {
      console.error("Topluluk bulunamadı");
      return null; // Değer döndürülüyor
    }
    const communityCreatorUid = communitySnapshot.get("creatorUid") as string || ""
    if (communityCreatorUid) {
      if (communityCreatorUid == memberUid) {
        console.error("Topluluk oluşturuldu");
        return null; // Değer döndürülüyor
      }
    }
    const communityName = communitySnapshot.get("name") as string;
    const onlyByRequest = communitySnapshot.get("participationByRequestOnly") as boolean;
    try {
      if (onlyByRequest) {
        // İstek yapan kullanıcı bilgilerini al
        const newMemberSnapshot = await db.collection('users').doc(memberUid).get();
        const memberToken = newMemberSnapshot.get("fcmToken") as string | undefined;
        const memberLang = newMemberSnapshot.get("language") as string || 'en';

        let title;
        let body;
        switch (memberLang) {
          case 'tr':
            title = `${communityName}`;
            body = `${communityName} topluluğuna katılım isteğin kabul edildi.`;
            break;
          default:
            title = `${communityName}`;
            body = `Your participation request for ${communityName} community has accepted.`;
            break;
        }

        // Bildirim gönder
        if (memberToken) {
          await sendCommunityNotification(
            memberToken,
            title,
            body,
            communityId,
            "community_participation",
            "OPEN_COMMUNITY",
            "high");
        } else {
          console.log(`FCM token bulunamadı: ${memberUid}`);
        }
      } else {
        const adminsSnapshot = await admin
          .firestore()
          .collection(`communities/${communityId}/members`)
          .where('rolePriority', 'in', [0, 1])
          .get();
        console.log(`bildirim gonderilecek kisi sayisi: ${adminsSnapshot.size}`);



        // Katılımcıların fcmToken'larını alıp, bildirim göndermek için Promise.all ile paralel işleme
        const promises = adminsSnapshot.docs.map(async (doc) => {
          const adminUid = doc.id; // Katılımcı UID'si

          // Katılımcının fcmToken'ını almak için 'users' koleksiyonunu sorguluyoruz
          const userDoc = await admin
            .firestore()
            .collection("users")
            .doc(adminUid)
            .get();

          const adminToken = userDoc.get("fcmToken") as string || "";
          const adminLang = userDoc.get("language") as string || 'en';

          let title;
          let body;
          switch (adminLang) {
            case "tr":
              title = "Yeni Üye"
              body = `${communityName} topluluğunda yeni kişiler var.`
              break;

            default:
              title = "New Members"
              body = `New members in ${communityName} community.`
              break;
          }

          if (adminToken) {
            // Katılımcıya bildirim gönderme
            return sendCommunityNotification(
              adminToken,
              title,
              body,
              communityId,
              "community_participation",
              "OPEN_COMMUNITY",
              "normal");
          } else {
            console.log(`FCM token bulunamadi: ${adminUid}`);
            return null;
          }
        });

        // Tüm bildirim gönderme işlemlerini paralel olarak yap
        await Promise.all(promises);
        console.log("Tüm katilimcilara bildirim gönderildi");
        return null;
      }
      return null;
    } catch (error) {
      console.log(`Bildirim gönderilemedi: ${error}`);
      return null;
    }
  });

export const onNewRequestForCommunity = functions.firestore
  .onDocumentCreated("communities/{communityId}/joiningRequests/{requestId}", async (event) => {
    const communityId = event.params.communityId;

    if (!communityId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }


    // Etkinlik bilgilerini al
    const communitySnapshot = await db.collection('communities').doc(communityId).get();
    if (!communitySnapshot.exists) {
      console.error("Topluluk bulunamadı");
      return null; // Değer döndürülüyor
    }
    const communityName = communitySnapshot.get("name") as string;
    try {
      const adminsSnapshot = await admin
        .firestore()
        .collection(`communities/${communityId}/members`)
        .where('rolePriority', 'in', [0, 1])
        .get();
      console.log(`bildirim gonderilecek kisi sayisi: ${adminsSnapshot.size}`);



      // Katılımcıların fcmToken'larını alıp, bildirim göndermek için Promise.all ile paralel işleme
      const promises = adminsSnapshot.docs.map(async (doc) => {
        const adminUid = doc.id; // Katılımcı UID'si

        // Katılımcının fcmToken'ını almak için 'users' koleksiyonunu sorguluyoruz
        const userDoc = await admin
          .firestore()
          .collection("users")
          .doc(adminUid)
          .get();

        const adminToken = userDoc.get("fcmToken") as string || "";
        const adminLang = userDoc.get("language") as string || 'en';

        let title;
        let body;
        switch (adminLang) {
          case "tr":
            title = "Yeni Katılım Talebi"
            body = `${communityName} topluluğuna katılmak isteyen yeni kişiler var.`
            break;

          default:
            title = "New Request"
            body = `There are new people wanting to join the ${communityName} community.`
            break;
        }

        if (adminToken) {
          // Katılımcıya bildirim gönderme
          return sendCommunityNotification(
            adminToken,
            title,
            body,
            communityId,
            "community_request",
            "OPEN_COMMUNITY",
            "normal");
        } else {
          console.log(`FCM token bulunamadi: ${adminUid}`);
          return null;
        }
      });

      // Tüm bildirim gönderme işlemlerini paralel olarak yap
      await Promise.all(promises);
      console.log("Tüm katilimcilara bildirim gönderildi");
      return null;

    } catch (error) {
      console.log(`Bildirim gönderilemedi: ${error}`);
      return null;
    }
  });

export const onPostLiked = functions.firestore
  .onDocumentCreated("communities/{communityId}/posts/{postId}/likes/{likeId}", async (event) => {
    const postId = event.params.postId;
    const communityId = event.params.communityId;
    const likedUser = event.params.likeId
    const likedUsername = event.data?.data().likedUsername;

    if (!postId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }

    try {
      // Post bilgilerini al
      const postSnapshot = await db.collection('communities').doc(communityId).collection('posts').doc(postId).get();
      if (!postSnapshot.exists) {
        console.error("post bulunamadı");
        return null; // Değer döndürülüyor
      }

      const postOwnerUid = postSnapshot.get("uid") as string;

      if (postOwnerUid == likedUser) {
        console.error("post sahibi beğendi");
        return null
      }


      // Etkinlik sahibinin bilgilerini al
      const postOwnerSnapshot = await db.collection('users').doc(postOwnerUid).get();
      const postOwnerToken = postOwnerSnapshot.get("fcmToken") as string;
      const postOwnerLanguage = postOwnerSnapshot.get("language") as string || 'en';

      // Bildirim metni oluştur
      let title;
      let body;
      switch (postOwnerLanguage) {
        case 'tr':
          title = `❤️`;
          body = `${likedUsername} gönderinizi beğendi.`;
          break;
        default:
          title = `❤️`;
          body = `${likedUsername} liked your post.`;
          break;
      }

      // Bildirim gönder
      if (postOwnerToken) {
        await sendPostNotification(
          postOwnerToken,
          title,
          body,
          communityId,
          postId,
          "post_liked",
          "OPEN_POST",
          "normal");
        return null; // Başarılı bir şekilde tamamlandığında da null döndürülüyor
      } else {
        console.log(`FCM token bulunamadı: ${postOwnerUid}`);
        return null; // Değer döndürülüyor
      }
    } catch (error) {
      console.error("Bildirim gönderirken hata oluştu:", error);
      return null; // Hata durumunda da değer döndürülüyor
    }
  });
export const onPostComment = functions.firestore
  .onDocumentCreated("communities/{communityId}/posts/{postId}/comments/{commentId}", async (event) => {
    const comment = event.data?.data();
    const postId = event.params.postId;
    const communityId = event.params.communityId;
    const commentOwnerUid = comment?.senderUid;
    const senderUsername = comment?.senderUserName;
    const messageContent = comment?.commentText;
    if (!postId) {
      console.log("Gerekli veriler eksik");
      return null; // Değer döndürülüyor
    }

    try {
      // Post bilgilerini al
      const postSnapshot = await db.collection('communities').doc(communityId).collection('posts').doc(postId).get();
      if (!postSnapshot.exists) {
        console.error("post bulunamadı");
        return null; // Değer döndürülüyor
      }

      const postOwnerUid = postSnapshot.get("uid") as string;
      if (postOwnerUid == commentOwnerUid) {
        console.error("kullanıcı kendi postuna yorum yaptı");
        return null
      }


      // Etkinlik sahibinin bilgilerini al
      const postOwnerSnapshot = await db.collection('users').doc(postOwnerUid).get();
      const postOwnerToken = postOwnerSnapshot.get("fcmToken") as string;
      const postOwnerLanguage = postOwnerSnapshot.get("language") as string || 'en';

      // Bildirim metni oluştur
      let title;
      let body;
      switch (postOwnerLanguage) {
        case 'tr':
          title = `Gönderinize Yorum Yapıldı`;
          body = `${senderUsername}: ${messageContent}`;
          break;
        default:
          title = `Your Post Has Been Commented On.`;
          body = `${senderUsername}: ${messageContent}`;
          break;
      }

      // Bildirim gönder
      if (postOwnerToken) {
        await sendPostNotification(
          postOwnerToken,
          title,
          body,
          communityId,
          postId,
          "post_comment",
          "OPEN_POST",
          "high");
        return null; // Başarılı bir şekilde tamamlandığında da null döndürülüyor
      } else {
        console.log(`FCM token bulunamadı: ${postOwnerUid}`);
        return null; // Değer döndürülüyor
      }
    } catch (error) {
      console.error("Bildirim gönderirken hata oluştu:", error);
      return null; // Hata durumunda da değer döndürülüyor
    }
  });

// Bildirim gönderme fonksiyonu
const sendEventNotification = async (
  token: string,
  title: string,
  body: string,
  eventId: string,
  type: string,
  clickAction: string,
  priority: "high" | "normal") => {

  const message = {
    token,
    notification: {
      title,
      body
    },
    data: {
      eventId,
      type
    },
    android: {
      notification: {
        clickAction
      },
      priority
    }
  };

  try {
    await admin.messaging().send(message);
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
  }
};

const sendChatNotification = async (
  token: string,
  title: string,
  body: string,
  eventId: string,
  type: string,
  clickAction: string,
  priority: "high" | "normal") => {

  const message = {
    token,
    notification: {
      title,
      body
    },
    data: {
      eventId,
      type
    },
    android: {
      notification: {
        clickAction
      },
      collapseKey: eventId,
      priority
    }

  };

  try {
    await admin.messaging().send(message);
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
  }
};
const sendCommunityNotification = async (
  token: string,
  title: string,
  body: string,
  communityId: string,
  type: string,
  clickAction: string,
  priority: "high" | "normal") => {

  const message = {
    token,
    notification: {
      title,
      body
    },
    data: {
      communityId,
      type
    },
    android: {
      notification: {
        clickAction
      },
      priority
    }

  };

  try {
    await admin.messaging().send(message);
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
  }
};
const sendPostNotification = async (
  token: string,
  title: string,
  body: string,
  communityId: string,
  postId: string,
  type: string,
  clickAction: string,
  priority: "high" | "normal") => {

  const message = {
    token,
    notification: {
      title,
      body
    },
    data: {
      communityId,
      postId,
      type
    },
    android: {
      notification: {
        clickAction
      },
      collapseKey: postId,
      priority
    }

  };

  try {
    await admin.messaging().send(message);
  } catch (error) {
    console.error("Bildirim gönderme hatası:", error);
  }
};