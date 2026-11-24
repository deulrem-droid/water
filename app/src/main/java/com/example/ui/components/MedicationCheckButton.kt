package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.IvoryCardBorder
import com.example.ui.theme.IvoryCardSurface
import com.example.ui.theme.MedDoneBadgeBg
import com.example.ui.theme.MedDoneBadgeText
import com.example.ui.theme.MedDoneGreen
import com.example.ui.theme.MedDoneGreenBorder
import com.example.ui.theme.PureWhite
import com.example.ui.theme.WarmInk
import com.example.ui.theme.WarmInkSecondary

@Composable
fun MedicationCheckButton(
  title: String,
  icon: ImageVector,
  iconContentDescription: String,
  iconTint: Color,
  iconContainerColor: Color,
  isTaken: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  val containerColor by
    animateColorAsState(
      targetValue = if (isTaken) MedDoneGreen else IvoryCardSurface,
      animationSpec = tween(durationMillis = 240),
      label = "medButtonBg",
    )

  val borderColor by
    animateColorAsState(
      targetValue = if (isTaken) MedDoneGreenBorder else IvoryCardBorder,
      animationSpec = tween(durationMillis = 240),
      label = "medButtonBorder",
    )

  val titleColor by
    animateColorAsState(
      targetValue = if (isTaken) PureWhite else WarmInk,
      animationSpec = tween(durationMillis = 240),
      label = "medTitleColor",
    )

  val doneText = stringResource(R.string.med_status_done)
  val doneHintText = stringResource(R.string.med_status_done_hint)
  val pendingText = stringResource(R.string.med_status_pending)

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      onClick()
    },
    modifier =
      modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = 96.dp)
        .testTag(testTag)
        .semantics {
          role = Role.Checkbox
          stateDescription = if (isTaken) doneText else pendingText
        },
    shape = RoundedCornerShape(24.dp),
    color = containerColor,
    border = BorderStroke(width = 2.dp, color = borderColor),
    shadowElevation = if (isTaken) 4.dp else 2.dp,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier =
            Modifier.size(56.dp)
              .clip(CircleShape)
              .background(
                if (isTaken) PureWhite.copy(alpha = 0.20f) else iconContainerColor
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = iconContentDescription,
            tint = if (isTaken) PureWhite else iconTint,
            modifier = Modifier.size(34.dp),
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = titleColor,
          )
          Text(
            text = if (isTaken) doneHintText else pendingText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isTaken) FontWeight.Bold else FontWeight.Medium,
            color = if (isTaken) PureWhite.copy(alpha = 0.95f) else WarmInkSecondary,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      if (isTaken) {
        AnimatedVisibility(
          visible = true,
          enter = scaleIn(tween(180)) + fadeIn(tween(180)),
          exit = fadeOut(tween(120)),
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MedDoneBadgeBg,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = stringResource(R.string.cd_check_done),
                tint = MedDoneBadgeText,
                modifier = Modifier.size(30.dp),
              )
              Text(
                text = doneText,
                style = MaterialTheme.typography.labelLarge,
                color = MedDoneBadgeText,
              )
            }
          }
        }
      } else {
        Icon(
          imageVector = Icons.Rounded.RadioButtonUnchecked,
          contentDescription = stringResource(R.string.med_action_tap_to_check),
          tint = IvoryCardBorder,
          modifier = Modifier.size(38.dp),
        )
      }
    }
  }
}
