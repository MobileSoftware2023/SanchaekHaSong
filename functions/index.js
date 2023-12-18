/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// const {onRequest} = require("firebase-functions/v2/https");
// const logger = require("firebase-functions/logger");

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.calculateAndNotifyRankings = functions.pubsub.schedule("45 13 * * 1") // 매주 월요일 자정
    .timeZone("Asia/Seoul")
    .onRun(async (context) => {
      try {
        // @ranking_college 초기화
        const rankingSnapshot = await admin.database().ref("@ranking_college").orderByValue().once("value");
        const orderedRankingList = [];
        rankingSnapshot.forEach((collegeSnapshot) => {
          const collegeName = collegeSnapshot.key;
          const walkCount = collegeSnapshot.val();
          orderedRankingList.push({collegeName, walkCount});
        });

        // 예시: 상위 3팀에게 알림 보내기
        const top3Teams = orderedRankingList
            .sort((a, b) => b.walkCount - a.walkCount)
            .slice(0, 3); // 상위 3팀 선택

        for (const team of top3Teams) {
          const tokens = await getDeviceTokensForTeamMembers([team.collegeName]);
          const message = {
            data: {
              title: "주간 랭킹 알림", // 알림 제목 수정
              body: `축하합니다! ${team.collegeName}은(는) 주간 랭킹 상위 3팀에 들었습니다.`,
              point: calculatePointsForTeam(top3Teams.indexOf(team) + 1), // 각 팀에 대한 포인트 계산
            },
          };

          if (tokens && tokens.length > 0) {
            await admin.messaging().sendToDevice(tokens, message);
          }
        }
        // @ranking 산정
        const usersSnapshot = await admin.database().ref("@ranking").orderByValue().once("value");
        const userRankingList = [];
        usersSnapshot.forEach((userSnapshot) => {
          const username = userSnapshot.key;
          const sumWalkCount = userSnapshot.child("sumWalkCount").val() || 0;
          userRankingList.push({username, sumWalkCount});
        },
        );

        // 예시: 상위 3명에게 알림 보내기
        const top3Users = userRankingList
            .sort((a, b) => b.sumWalkCount - a.sumWalkCount)
            .slice(0, 3);

        for (const user of top3Users) {
          const tokens = await getDeviceTokensForUsers([user.username]);
          const message = {
            data: {
              title: "주간 랭킹 알림", // 알림 제목 수정
              body: `축하합니다! ${user.username}님은 주간 랭킹 상위 3인 안에 들었습니다.`,
              point: calculatePointsForUser(top3Users.indexOf(user) + 1), // 각 사용자에 대한 포인트 계산
            },
          };

          if (tokens && tokens.length > 0) {
            await admin.messaging().sendToDevice(tokens, message);
          }
        }
        // 3. 걸음수 초기화
        await resetWalkCounts();
      } catch (error) {
        console.error("Error calculating and notifying rankings:", error);
        return null;
      }
    });
// 사용자들의 FCM 토큰을 가져오는 함수
async function getDeviceTokensForUsers(usernames) {
  const tokens = [];
  for (const username of usernames) {
    const snapshot = await admin.database().ref(`${username}/fcmToken`).once("value");
    const token = snapshot.val();
    if (token) {
      tokens.push(token);
    }
  }
  return tokens;
}

async function getDeviceTokensForTeamMembers(collegeName) {
  const tokens = [];
  const usersSnapshot = await admin.database().ref("/").once("value");
  const users = [];
  usersSnapshot.forEach((childSnapshot) => {
    if (childSnapshot.key !== "@college" && childSnapshot.key !== "@college_walkCount" && childSnapshot.key !== "@ranking" && childSnapshot.key !== "@ranking_college") {
      users.push(childSnapshot.key);
    }
  });
  // 상위 3팀의 사용자를 가져와서 FCM 토큰을 추출
  const top3Usernames = [];
  for (const user of users) {
    const userCollege = await admin.database().ref(`$user/college`);
    if (userCollege === collegeName) {
      top3Usernames.push(user);
    }
  }

  // 각 사용자별로 FCM 토큰을 가져오고, 해당 사용자가 1, 2, 3등 팀에 속해 있다면 토큰 추가
  for (const user of top3Usernames) {
    const fcmTokenSnapshot = await admin.database().ref(`${user}/fcmToken`).once("value");
    const fcmToken = fcmTokenSnapshot.val();

    tokens.push(fcmToken);
  }
  return tokens;
}


// 사용자에게 알림을 보내는 함수
async function sendNotificationToUser(userId, message) {
  // 사용자의 FCM 토큰을 가져오는 로직 추가
  const userSnapshot = await admin.database().ref(`${userId}`).once("value");
  const fcmToken = userSnapshot.child("fcmToken").val();

  if (fcmToken) {
    // 알림 보내기
    const payload = {
      notification: {
        title: "랭킹 알림",
        body: message,
      },
      data: {
        // 사용자에게 전달할 추가 데이터 (예: 포인트)
        points: calculatePointsForUserRanking(userId),
      },
    };

    await admin.messaging().sendToDevice(fcmToken, payload);
  }
}
// 사용자에게 포인트를 부여하는 함수
function calculatePointsForTeam(rank) {
  // 팀의 랭킹에 따른 포인트 계산 로직
  if (rank === 1) {
    return 1000;
  } else if (rank === 2) {
    return 700;
  } else if (rank === 3) {
    return 400;
  } else {
    return 0; // 상위 3팀 이외에는 0 포인트
  }
}

function calculatePointsForUser(rank) {
  // 사용자 랭킹에 따른 포인트 계산 로직
  if (rank === 1) {
    return 1000;
  } else if (rank === 2) {
    return 700;
  } else if (rank === 3) {
    return 400;
  } else {
    return 0; // 상위 3명 이외에는 0 포인트
  }
}


exports.resetWalkCounts = functions.pubsub.schedule("45 13 * * 1") // 매주 월요일 자정
    .timeZone("Asia/Seoul")
    .onRun(async (context) => {
      try {
        // @college_walkCount 초기화
        const collegeWalkCountSnapshot = await admin.database().ref("/@college_walkCount").once("value");
        const collegeWalkCountResetPromises = [];
        collegeWalkCountSnapshot.forEach((childSnapshot) => {
          collegeWalkCountResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(collegeWalkCountResetPromises);

        // @ranking 초기화
        const rankingSnapshot = await admin.database().ref("/@ranking").once("value");
        const rankingResetPromises = [];
        rankingSnapshot.forEach((childSnapshot) => {
          rankingResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(rankingResetPromises);

        // @ranking_college 초기화
        const rankingCollegeSnapshot = await admin.database().ref("/@ranking_college").once("value");
        const rankingCollegeResetPromises = [];
        rankingCollegeSnapshot.forEach((childSnapshot) => {
          rankingCollegeResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(rankingCollegeResetPromises);

        // 개별 사용자의 sumWalkCount 초기화
        const usersSnapshot = await admin.database().ref("/").once("value");
        const userResetPromises = [];
        usersSnapshot.forEach((childSnapshot) => {
          if (childSnapshot.key !== "@college" && childSnapshot.key !== "@college_walkCount" && childSnapshot.key !== "@ranking" && childSnapshot.key !== "@ranking_college") {
            userResetPromises.push(childSnapshot.child("sumWalkCount").ref.set(0));
          }
        });
        await Promise.all(userResetPromises);

        console.log("Walk counts reset successfully.");
        return null;
      } catch (error) {
        console.error("Error resetting walk counts:", error);
        return null;
      }
    });

exports.resetDailyQuest = functions.pubsub.schedule("45 13 * * *") // 매일 자정
    .timeZone("Asia/Seoul")
    .onRun(async (context) => {
      try {
        // 일일 퀘스트 초기화
        await resetDailyQuests();

        console.log("Daily quest reset successfully.");
        return null;
      } catch (error) {
        console.error("Error resetting daily quest:", error);
        return null;
      }
    });

async function resetDailyQuests() {
  // 매일 자정 일일 퀘스트 초기화 로직
  try {
    const usersSnapshot = await admin.database().ref("/").once("value");

    // 모든 사용자의 일일 퀘스트 초기화
    const resetPromises = [];
    usersSnapshot.forEach((childSnapshot) => {
      if (childSnapshot.key !== "@college" && childSnapshot.key !== "@college_walkCount" && childSnapshot.key !== "@ranking" && childSnapshot.key !== "@ranking_college") {
        const username = childSnapshot.key;

        const isCompletedClickedRef = admin.database().ref(`/${username}/dailyQuest/isCompletedClicked`);
        const isCompletedRef = admin.database().ref(`/${username}/dailyQuest/isCompleted`);

        // isCompletedClicked 초기화
        resetPromises.push(isCompletedClickedRef.set(false));

        // isCompleted 초기화
        resetPromises.push(isCompletedRef.set(false));
      }
    });

    // 모든 작업을 기다림
    await Promise.all(resetPromises);

    console.log("Daily quests reset successfully.");
    return null;
  } catch (error) {
    console.error("Error resetting daily quests:", error);
    return null;
  }
}


// 사용자가 로그인할 때 호출되는 함수
exports.saveFCMTokenToServer = functions.https.onCall((data, context) => {
  const userId = data.userId;
  const fcmToken = data.fcmToken;

  if (!userId || !fcmToken) {
    return {status: "error", message: "Invalid parameters"};
  }

  // Realtime Database에 FCM 토큰 저장
  return admin.database().ref(`${userId}/fcmToken`).set(fcmToken)
      .then(() => {
        return {status: "success", message: "FCM token saved successfully"};
      })
      .catch((error) => {
        return {status: "error", message: "Error saving FCM token", error: error};
      });
});

// FCM 토큰을 서버에 저장하는 함수
async function saveFCMToken(userId, fcmToken) {
  try {
    // userId와 fcmToken을 서버에 저장
    // 이때 사용자를 식별하기 위해 userId를 사용
    await admin.database().ref(`${userId}/fcmToken`).set(fcmToken);
    console.log("FCM Token saved successfully.");
  } catch (error) {
    console.error("Error saving FCM Token:", error);
  }
}
// 이벤트 리스너 등록
firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    const userId = user.uid;
    const fcmToken = "...";

    // 사용자가 로그인할 때마다 FCM 토큰을 업데이트
    saveFCMToken(userId, fcmToken);
  } else {
    // 사용자가 로그아웃한 경우
    console.log("사용자가 로그아웃했습니다.");
  }
});

