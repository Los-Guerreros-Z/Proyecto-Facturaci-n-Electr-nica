package application.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, ClienteResponse cliente) {
}
