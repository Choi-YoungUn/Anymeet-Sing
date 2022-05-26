-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: i6a505.p.ssafy.io    Database: anymeetsong
-- ------------------------------------------------------
-- Server version	8.0.28

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `favorite_songs`
--

DROP TABLE IF EXISTS `favorite_songs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_songs` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(30) NOT NULL,
  `song_name` varchar(45) NOT NULL,
  `song_url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_favorite_song_user1_idx` (`user_id`),
  CONSTRAINT `FK_favoritesongs_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite_songs`
--

LOCK TABLES `favorite_songs` WRITE;
/*!40000 ALTER TABLE `favorite_songs` DISABLE KEYS */;
INSERT INTO `favorite_songs` VALUES (1,'cjftn121@gmail.com','counting star','https://www.youtube.com/watch?v=kGkUdDRC9mE&ab_channel=LyricsByNithi'),(2,'dnffk@hotmail.com','perfect','https://www.youtube.com/watch?v=XXww1SikEMY&ab_channel=orangejellybean'),(3,'ssafy@gmail.com','붉은노을','https://www.youtube.com/watch?v=_S1uqvuO43k&ab_channel=%EC%95%A4%ED%95%B4%EC%8A%A4');
/*!40000 ALTER TABLE `favorite_songs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questions` (
  `id` int unsigned NOT NULL,
  `question` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions` DISABLE KEYS */;
INSERT INTO `questions` VALUES (1,'현재 무슨 캠퍼스에 계신가요?'),(2,'가장 좋아하는 영화는 무엇인가요?'),(3,'가장 좋아하는 음식은 무엇인가요?'),(4,'고향이 어디?');
/*!40000 ALTER TABLE `questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(45) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `host_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=257 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (89,'noraebang','','cjftn121@gmail.com'),(90,'zilla','1234','dnffk@hotmail.com'),(91,'korea','5312','ssafy@gmail.com');
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room_participant`
--

DROP TABLE IF EXISTS `room_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `room_participant` (
  `id` int unsigned NOT NULL,
  `user_id` varchar(45) NOT NULL,
  PRIMARY KEY (`id`,`user_id`),
  KEY `users_room_idx` (`user_id`),
  CONSTRAINT `FK_participant_room` FOREIGN KEY (`id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room_participant`
--

LOCK TABLES `room_participant` WRITE;
/*!40000 ALTER TABLE `room_participant` DISABLE KEYS */;
INSERT INTO `room_participant` VALUES (89,'cjftn121@gmail.com'),(90,'cjftn121@gmail.com'),(91,'cjftn121@gmail.com'),(91,'dnffk@hotmail.com');
/*!40000 ALTER TABLE `room_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `song_list`
--

DROP TABLE IF EXISTS `song_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `song_list` (
  `song_id` int unsigned NOT NULL AUTO_INCREMENT,
  `room_id` int unsigned NOT NULL,
  `song_name` varchar(255) NOT NULL,
  `song_url` varchar(255) NOT NULL,
  `song_thumbnail` varchar(255) DEFAULT NULL,
  `id` varchar(255) NOT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`song_id`,`room_id`),
  KEY `FK_songlist_room_idx` (`room_id`),
  CONSTRAINT `FK_songlist_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=66 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `song_list`
--

LOCK TABLES `song_list` WRITE;
/*!40000 ALTER TABLE `song_list` DISABLE KEYS */;
INSERT INTO `song_list` VALUES (26,90,'perfect','https://www.youtube.com/watch?v=XXww1SikEMY&ab_channel=orangejellybean','0','dnffk@hotmail.com','둘리'),(27,91,'counting star','https://www.youtube.com/watch?v=kGkUdDRC9mE&ab_channel=LyricsByNithi','0','cjftn121@gmail.com','철수');
/*!40000 ALTER TABLE `song_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` varchar(255) NOT NULL,
  `questions_id` int unsigned NOT NULL,
  `password` varchar(255) NOT NULL,
  `nickname` varchar(30) NOT NULL,
  `phone` varchar(45) NOT NULL,
  `answer` varchar(45) NOT NULL,
  `is_blocked` tinyint DEFAULT '0',
  `is_admin` tinyint DEFAULT '0',
  `listen_count` int unsigned DEFAULT '0',
  `singing_count` int unsigned DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_user_questions_idx` (`questions_id`),
  CONSTRAINT `FK_user_questions` FOREIGN KEY (`questions_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('cjftn121@gmail.com',3,'$2a$10$6CRjJAO4SFrLSMKqtsw1DuMDK3xa0eFPi9EAeavMt77RJjbGuihey','철수','01041674482','햄버거',0,0,4,2),('coach@ssafy.com',1,'$2a$10$m7pn17ckywSKOpOOC/FOie6cXmP3L.FkZ7RFSQLQho34/EdTGhpTm','코치','01012341234','서울',0,0,0,0),('consultant@ssafy.com',1,'$2a$10$C65ZK3XHUseCKGuujEUZdezSnH8iEEm6gDSuG12TbLxMxGEVhHYSW','컨설턴트','01012341234','서울',0,0,0,0),('darkstar0406@daum.net',4,'$2a$10$68rjg0SCsbMbBDDsE.jESu4BTy.4VkpfGwr5KWuIFuqQ/ag.2N7W.','Blue','0102001122','의왕',0,0,0,0),('darkstar0406@naver.com',4,'$2a$10$4Fo77pfoQv8DiUcsl4Ku1eF1wlOndS6oPTHuIE0MQnIzu8.8JiGam','TOM','01051884469','서울',0,0,0,0),('darkstaryoung0406@gmail.com',1,'$2a$10$nNeSLd3/B3GPDNRRLajb3e0O1XAcyURpQ7dOzx/FiS.GC20qhXIG2','youtube','01051884478','부산',0,0,0,0),('dnffk@hotmail.com',1,'$2a$10$LFNA4LBOgPQ/WK2dCWAHbORirKg8JPIx30sZCkPMykoY7jEzrTBia','둘리','01021344233','서울',1,0,2,3),('kimyj1288@naver.com',1,'$2a$10$9H4tAVFzr7LKSWPoVyNkiOwWyh8P84vdG7mF5fey42w/gvv7czbZO','김영진','01026262626','서울',0,0,0,0),('rudguq439@naver.com',1,'$2a$10$6R0rIERqZ4BpsaoXGjyMiuI/QCG5jUQovL2mHHsIbW40uL4czvoQe','협스','01012341234','서울',0,0,0,0),('ssafy.a505@gmail.com',1,'$2a$10$iFQsDW8u5TTW3m02htlQV.MD8C31iHacXHo3x/Jisc5zo9G7vVm3e','관리자','01031249253','서울',0,1,0,0),('ssafy@gmail.com',2,'$2a$10$oOGEBmI2Vt.V7BJWK4vgJOjF/YHOCkh3B0hu0NrkMkaLVuasNuOuS','영희','01091524192','덤앤더머',0,0,5,1),('test@test.com',1,'$2a$10$WoMH1eGAD1pdZyZgMKhwsOFNOhTNj7H7L1QwLIMkMqMC1s3nSG4z2','JYH','010-2345-6789','test',0,0,0,0),('verify.prj@gmail.com',1,'$2a$10$AVuljeR43rAUMBFZtjouHOHxPGJLZ4TxEQDCwXpgkBaub7uhPRXge','관리자','01041959193','서울',0,1,0,0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `verify_code`
--

DROP TABLE IF EXISTS `verify_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `verify_code` (
  `id` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `insert_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `verify_code`
--

LOCK TABLES `verify_code` WRITE;
/*!40000 ALTER TABLE `verify_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `verify_code` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-02-18 11:08:51
