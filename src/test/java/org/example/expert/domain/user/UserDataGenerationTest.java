package org.example.expert.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class UserDataGenerationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int TOTAL_USERS = 1_000_000;
    private static final int BATCH_SIZE = 5000;
    private static final String password = "123456789aA!";
    private static final String userRole = "USER";

    @Test
    public void generateLargeUserData() {
        List<Object[]> batchArgs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < TOTAL_USERS; i++) {
            String email = "user" + i + "@example.com";
            String nickname = generateRandomKoreanNickname(i); // 고유 번호를 추가하여 중복 방지
            Object[] userData = new Object[]{email, password, userRole, nickname, now, now};
            batchArgs.add(userData);

            // 배치 크기에 도달할 때마다 DB에 저장
            if (i > 0 && i % BATCH_SIZE == 0) {
                executeBatchInsert(batchArgs);
                batchArgs.clear();
                System.out.println("Batch " + i / BATCH_SIZE + " completed.");
            }
        }

        // 마지막 남은 데이터 저장
        if (!batchArgs.isEmpty()) {
            executeBatchInsert(batchArgs);
        }

        System.out.println("모든 사용자 생성 완료");
    }

    private void executeBatchInsert(List<Object[]> batchArgs) {
        // 트랜잭션 사용으로 자동 커밋 방지
        jdbcTemplate.execute((Connection connection) -> {
            connection.setAutoCommit(false); // 자동 커밋 끄기
            String sql = "INSERT INTO users (email, password, user_role, nickname, created_at, modified_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Object[] args : batchArgs) {
                    pstmt.setString(1, (String) args[0]);
                    pstmt.setString(2, (String) args[1]);
                    pstmt.setString(3, (String) args[2]);
                    pstmt.setString(4, (String) args[3]);
                    pstmt.setTimestamp(5, Timestamp.valueOf((LocalDateTime) args[4]));
                    pstmt.setTimestamp(6, Timestamp.valueOf((LocalDateTime) args[5]));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            connection.commit(); // 배치 완료 후 수동 커밋
            return null;
        });
    }

    // 유의미한 랜덤 한글 닉네임 생성 + 고유 번호 추가
    private String generateRandomKoreanNickname(int uniqueNumber) {
        String[] firstNames = {"철", "수", "민", "영", "지", "준", "서", "하"};
        String[] lastNames = {"김", "이", "박", "최", "정", "강", "윤", "조"};

        Random random = new Random();
        String firstName = firstNames[random.nextInt(firstNames.length)];
        String lastName = lastNames[random.nextInt(lastNames.length)];

        return lastName + firstName + uniqueNumber;
    }
}
