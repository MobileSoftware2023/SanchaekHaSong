/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */


// The Firebase Admin SDK to access the Firebase Realtime Database.
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.calculateAndNotifyRankings =
    functions.pubsub.schedule("0 0 * * 1") // 매주 월요일 자정
        .timeZone("Asia/Seoul")
        .onRun(async (context) => {
          try {
            // @ranking_college 초기화
            const rankingSnapshot =
             await admin.database().ref("/@ranking_college")
                 .orderByValue().once("value");
            const orderedRankingList = [];
            rankingSnapshot.forEach((collegeSnapshot) => {
              const collegeName = collegeSnapshot.key;
              const walkCount = collegeSnapshot.val();
              orderedRankingList.push({collegeName, walkCount});
            });
            console.log("orderedRankingList", orderedRankingList);
            // 예시: 상위 3팀에게 알림 보내기
            const top3Teams = orderedRankingList
                .sort((a, b) => b.walkCount - a.walkCount)
                .slice(0, 3); // 상위 3팀 선택
            console.log("top3Teams", top3Teams);
            for (const team of top3Teams) {
              const tokens =
              await getDeviceTokensForTeamMembers([team.collegeName]);
              const message = {
                data: {
                  title: "주간 단과대 랭킹 알림", // 알림 제목 수정
                  body: `축하합니다! ${team.collegeName}은(는) 단과대 랭킹 상위 3팀에 들었습니다.`,
                  point:
                    `${calculatePointsForTeam(top3Teams.indexOf(team) + 1)}`,
                },
              };

              if (tokens && tokens.length > 0) {
                await admin.messaging().sendToDevice(tokens, message)
                    .then((response) => {
                      console.log("Successfully sent message:", tokens);
                    })
                    .catch((error) => {
                      console.log("Error sending message:", error);
                    });
              }
            }
            // @ranking 산정
            const usersSnapshot =
                await admin.database()
                    .ref("/@ranking").orderByValue().once("value");
            const userRankingList = [];
            usersSnapshot.forEach((userSnapshot) => {
              const username = userSnapshot.key;
              const sumWalkCount =
              userSnapshot.val() || 0;
              userRankingList.push({username, sumWalkCount});
            },
            );
            console.log("userRankingList", userRankingList);
            // 예시: 상위 3명에게 알림 보내기
            const top3Users = userRankingList
                .sort((a, b) => b.sumWalkCount - a.sumWalkCount)
                .slice(0, 3);
            console.log("top3Users", top3Users);
            for (const user of top3Users) {
              const tokens = await getDeviceTokensForUsers([user.username]);
              const message = {
                data: {
                  title: "주간 개인 랭킹 알림", // 알림 제목 수정
                  body: `축하합니다! ${user.username}님은 개인 랭킹 3인 안에 들었습니다.`,
                  point:
                  `${calculatePointsForUser(top3Users.indexOf(user) + 1)}`,

                },
              };

              if (tokens && tokens.length > 0) {
                await admin.messaging().sendToDevice(tokens, message)
                    .then((response) => {
                      console.log("Successfully sent message:", tokens);
                    })
                    .catch((error) => {
                      console.log("Error sending message:", error);
                    });
              }
            }
          } catch (error) {
            console.error("Error calculating and notifying rankings:", error);
            return null;
          }
        });

/**
 * 함수의 설명
 * @param {String} usernames - 매개변수1의 설명
 * @return {토큰배열} tokens
 */
async function getDeviceTokensForUsers(usernames) {
  const tokens = [];
  for (const username of usernames) {
    const snapshot =
    await admin.database().ref(`/${username}/fcmToken`).once("value");
    const token = snapshot.val();
    if (token) {
      tokens.push(token);
    }
  }
  return tokens;
}
/**
 * 함수의 설명
 * @param {String} collegeName
 * @return {토큰배열} tokens
 */
async function getDeviceTokensForTeamMembers(collegeName) {
  const tokens = [];
  const usersSnapshot = await admin.database().ref("/").once("value");
  const users = [];
  usersSnapshot.forEach((childSnapshot) => {
    if (childSnapshot.key !== "@college" &&
    childSnapshot.key !== "@college_walkCount" &&
    childSnapshot.key !== "@ranking" &&
    childSnapshot.key !== "@ranking_college") {
      users.push(childSnapshot.key);
    }
  });
  console.log("users", users);
  // 상위 3팀의 사용자를 가져와서 FCM 토큰을 추출
  const top3Usernames = [];
  for (const user of users) {
    const userRef = admin.database().ref(`/${user}`);
    try {
      const snapshot = await userRef.once("value");
      const userData = snapshot.val();
      console.log("userData.college", userData.college);
      console.log("collegeName", collegeName);
      if (userData) {
        const college = userData.college;
        if (college === collegeName[0]) {
          top3Usernames.push(user);
        }
      } else {
        console.log(`User ${user} not found`);
      }
    } catch (error) {
      console.error("Error fetching user data:", error);
    }
  }
  console.log("top3Usernames", top3Usernames);
  // 각 사용자별로 FCM 토큰을 가져오고, 해당 사용자가 1, 2, 3등 팀에 속해 있다면 토큰 추가
  for (const user of top3Usernames) {
    const fcmTokenSnapshot =
    await admin.database().ref(`/${user}/fcmToken`).once("value");
    const fcmToken = fcmTokenSnapshot.val();

    tokens.push(fcmToken);
  }
  return tokens;
}
/**
 * 함수의 설명
 * @param {Int} rank
 * @return {int} points
 */
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
/**
 * 함수의 설명
 * @param {Int} rank
 * @return {int} points
 */
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

exports.resetWalkCounts = functions.pubsub.schedule("0 0 * * 1") // 매주 월요일 자정
    .timeZone("Asia/Seoul")
    .onRun(async (context) => {
      try {
        // @college_walkCount 초기화
        const collegeWalkCountSnapshot =
        await admin.database().ref("/@college_walkCount").once("value");
        const collegeWalkCountResetPromises = [];
        collegeWalkCountSnapshot.forEach((childSnapshot) => {
          collegeWalkCountResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(collegeWalkCountResetPromises);

        // @ranking 초기화
        const rankingSnapshot =
        await admin.database().ref("/@ranking").once("value");
        const rankingResetPromises = [];
        rankingSnapshot.forEach((childSnapshot) => {
          rankingResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(rankingResetPromises);

        // @ranking_college 초기화
        const rankingCollegeSnapshot =
        await admin.database().ref("/@ranking_college").once("value");
        const rankingCollegeResetPromises = [];
        rankingCollegeSnapshot.forEach((childSnapshot) => {
          rankingCollegeResetPromises.push(childSnapshot.ref.set(0));
        });
        await Promise.all(rankingCollegeResetPromises);

        // 개별 사용자의 sumWalkCount 초기화
        const usersSnapshot = await admin.database().ref("/").once("value");
        const userResetPromises = [];
        usersSnapshot.forEach((childSnapshot) => {
          if (childSnapshot.key !== "@college" &&
          childSnapshot.key !== "@college_walkCount" &&
          childSnapshot.key !== "@ranking" &&
          childSnapshot.key !== "@ranking_college") {
            userResetPromises
                .push(childSnapshot.child("sumWalkCount").ref.set(0));
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

exports.resetDailyQuest = functions.pubsub.schedule("0 0 * * *") // 매일 자정
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
/**
 * 함수의 설명
 * @return {boolean} null
 */
async function resetDailyQuests() {
  // 매일 자정 일일 퀘스트 초기화 로직
  try {
    const usersSnapshot = await admin.database().ref("/").once("value");

    // 모든 사용자의 일일 퀘스트 초기화
    const resetPromises = [];
    usersSnapshot.forEach((childSnapshot) => {
      if (childSnapshot.key !== "@college" &&
      childSnapshot.key !== "@college_walkCount" &&
      childSnapshot.key !== "@ranking" &&
      childSnapshot.key !== "@ranking_college") {
        const username = childSnapshot.key;

        const isCompletedClickedRef =
        admin.database().ref(`/${username}/dailyQuest/isCompletedClicked`);
        const isCompletedRef =
        admin.database().ref(`/${username}/dailyQuest/isCompleted`);

        // isCompletedClicked 초기화
        resetPromises.push(isCompletedClickedRef.
            set([false, false, false, false, false, false]));

        // isCompleted 초기화
        resetPromises.push(isCompletedRef.
            set([false, false, false, false, false, false]));
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
  console.log(`유저 아이디 ${userId} 토큰: ${fcmToken}`);
  if (!userId || !fcmToken) {
    return {status: "error", message: "Invalid parameters"};
  }

  // Realtime Database에 FCM 토큰 저장
  return admin.database().ref(`/${userId}/fcmToken`).set(`${fcmToken}`)
      .then(() => {
        return {status: "success", message: "FCM token saved successfully"};
      })
      .catch((error) => {
        return {status: "error", message: "Error saving FCM ", error: error};
      });
});


