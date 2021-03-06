package org.springframework.data.arangodb.test.query;

import com.arangodb.entity.DocumentField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.LocalDate;

@Data
@EqualsAndHashCode(of="userId")
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@DocumentField(DocumentField.Type.KEY)
	private String userId;
	private String fullName;
	private LocalDate dateOfBirth;
	private boolean enabled;
}
