package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

/**
 * A beautiful, custom Material 3 Expressive SplitButton.
 * Groups a primary action ("Commit & Push") and a secondary preset message selector dropdown.
 */
@Composable
fun SplitButton(
    mainText: String,
    onMainClick: () -> Unit,
    onPresetSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    presets: List<String> = listOf(
        "feat: Add local asset folders",
        "fix: Correct document updates",
        "chore: Synchronize project assets",
        "docs: Update local repository readmes"
    )
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Action Button
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .clickable { onMainClick() }
                .testTag("commit_push_split_main"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mainText.uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.15.sp
                    ),
                    maxLines = 1
                )
            }
        }

        // Dividers
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
        )

        // Dropdown Arrow Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { dropdownExpanded = true }
                .testTag("commit_push_split_dropdown"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Quick Commit Messages",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Quick Select Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onPresetSelect(preset)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * A playful, connected ButtonGroup for selecting Git Branches as specified in MD3E.
 */
@Composable
fun BranchButtonGroup(
    branches: List<String>,
    selectedBranch: String,
    onBranchSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        branches.forEach { branch ->
            val isSelected = branch == selectedBranch
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            }

            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onBranchSelected(branch) }
                    .testTag("branch_button_$branch"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = branch,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Multi-step coach marks that guide the user with playful animated cartoon tooltips.
 */
@Composable
fun CoachMarksOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    Popup(onDismissRequest = {}) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(enabled = false) {}
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Energetic Icon and Bubble (Replacing emojis with Material Rounded icons as requested)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (step == 1) Icons.Rounded.FolderOpen else Icons.Rounded.CloudUpload,
                        contentDescription = "Step Icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (step == 1) "Pick Local Folder!" else "Commit & Push!",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (step == 1) {
                                "Tap the neon FAB at the bottom anytime to trigger SAF, letting you scan and select folders on your Android device securely."
                            } else {
                                "Press our Split Commit button! It packages the entire nested path, updates references, and creates a neat single commit effortlessly."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (step == 1) {
                                Button(
                                    onClick = { step = 2 },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Next Tip")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ChevronRight, "Next")
                                }
                            } else {
                                Button(
                                    onClick = { onDismiss() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("coachmark_done")
                                ) {
                                    Text("Let's Go!")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
